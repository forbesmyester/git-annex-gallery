(ns git-annex-gallery.core
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as file]
            [clojure.java.io :as io]
            [clojure.set :as clj-set :refer [difference]]
            [clojure.string :as str])
  (:gen-class))

(defn loose-end [coll] (reverse (rest (reverse coll))))

(defn get-possible-md-prelude [filename]
    (let [components (str/split filename #"\.")]
      (loop [my-components components
             result []]
        (if (= 0 (count my-components))
          result
          (recur (loose-end my-components) (conj result my-components))
          )
        )))

(defn remove-cache [cache-dir]
  (if (.exists (file/as-file cache-dir))
      (= 0 (:exit (shell/sh "rm" "-rf" cache-dir)))
      true))

(defn get-possible-md [filename]
  (if (re-find #"\.md$" filename)
    []
    (let [filter-edited #(not (= "edited" %))
          ]
      (->> (get-possible-md-prelude filename)
           (map #(filter filter-edited %) )
           (map #(str/join "." %) )
           (map #(str % ".md") )
           set
           )
      )
    ))

(defn list-edited-source [files]
  (map #(str/replace % #"\.edited" "" )
       (filter #(re-find #"\.edited\." %) files)))

(defn filter-files [files]
  (let [filter-out (->> files
                        (map get-possible-md)
                        (map vec)
                        flatten
                        vec
                        set
                        )]
    (difference (difference (set files) filter-out) (list-edited-source files))))

(defn drop-while-return-checked
  "
  Runs proc on items in data and returns a passing item, without evaluating anything else.

  Parameters
   * Function proc [element] A function to run on an elements of data
   * Function check [result-of-proc] The check that will be performe don the output of proc

  Returns a map in the form {:result the_result_of_proc :element the_element_that_passed_check :index the_index_of_colon_element}
  "
  ([proc check data n]
   (if (< n (count data))
     (let [r (proc (nth data n))]
       (if (check r)
         {:result r :element (nth data n) :index n }
         (drop-while-return-checked proc check data (inc n))))))
  ([proc check data] (drop-while-return-checked proc check data 0)))

(defn get-checksum [path]
  (let [dir (-> path file/as-file .getParent file/as-file .getPath)
        filename (-> path file/as-file .getName)
        get-checksum-config [{ :sh ["git" "annex" "lookupkey" filename :dir dir] :post str/trim }
                             { :sh ["git" "ls-files" "-s" filename :dir dir] :post #(second(str/split % #" +")) }
                             { :sh ["sha1sum" filename :dir dir] :post #(first(str/split % #" +")) }]
        result (drop-while-return-checked
                 #(apply shell/sh (:sh %))
                 #(= 0 (:exit %))
                 get-checksum-config)
        post (:post (:element result))
        out (:out (:result result))]
    (post out)))

(defn is-leaf-directory [d]
  (not (some #(.isDirectory %) (rest (file-seq d)))))

(defn identify-albums [root-path]
  (map #(.getPath %) (filter
    #(and (.isDirectory %) (is-leaf-directory %))
    (file-seq root-path))))

(defn identify-files [album-path]
  (let [is-file #(.isFile %)
        is-not-hidden #(not (= \. (first (.getName %))))
        file-filter (every-pred is-file is-not-hidden)]
    (filter-files
      (map #(.getPath %)
           (filter is-not-hidden  (.listFiles album-path))
           ))
    ))

(defn keyify [aliases]
  (let [get-key (fn [aliases str]
                  (if (aliases str)
                    (keyword (aliases str))
                    (keyword str)))
        hashify (fn [pair]
                  [(get-key aliases (first pair)) (second pair)])]
    (comp get-key hashify)))

(defn mega-mapper [aliases the-map]
  (if (= (count aliases) 0 ) the-map
    (let [exe (fn [[ks fun]]
                (fun ks (map the-map ks))
                )]
      (apply conj the-map (map exe aliases))
      )))

(defn tagify [field-seperator keyvalue-seperator aliases str]
  (let [split-to-fields #(str/split % field-seperator)
        split-key-and-value #(str/split % keyvalue-seperator 2)
        trimmer #(map str/trim %)
        combiner #(into {} (map vector (map first %) (map second %)))
        ]
  (mega-mapper aliases
    (->> str
      split-to-fields
      (map split-key-and-value)
      (map trimmer)
      (combiner)
      ))))

(defn extract-metadata [path]
  (let [cmd-output (shell/sh "exiv2" path)]
    (if (= 0 (:exit cmd-output))
      (tagify
        #"\n"
        #":"
        {
         ["Image timestamp"] (fn [ks vs] {:timestamp (first vs)})
         ["Image size"] (fn [ks vs] (let [[w h] (str/split (first vs) #"x")]
                                      {:resolution [(Integer. (str/trim w)) (Integer. (str/trim h))]}))
         }
        (:out cmd-output))
      (throw (Exception. (str "exiv2 did not return with exit code 0 from path '" path "'"))))))

(defn create-dir [path]
  (= 0 (:exit (shell/sh "mkdir" "-p" path))))

(defn get-thumbnail [width-height convert-to-format destination-directory source-file]
  (let [dest-file (io/file destination-directory (str width-height ".png"))
        dest-filename (.getPath dest-file)]
    (if (.exists dest-file)
      [dest-filename 0]
      (do (create-dir destination-directory)
          (if (= 0 (:exit (shell/sh "convert" "-format" convert-to-format "-thumbnail" width-height source-file dest-filename)))
            [dest-filename 1]
            nil)))))

(defn get-thumbnails [resolutions convert-to-format destination-directory source-file]
  (let [checksum (get-checksum source-file)
        thumb-dir (.getPath (io/file destination-directory checksum))]
    (map
      (fn [[width height]]
        (first (get-thumbnail
                 (str width "x" height)
                 convert-to-format
                 thumb-dir
                 source-file
                 )))
      resolutions)))

(defn process-album [resolutions convert-to-format destination-directory source-directory]
  (map 
    #(get-thumbnails resolutions convert-to-format destination-directory %)
    (identify-files source-directory)
    ))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
