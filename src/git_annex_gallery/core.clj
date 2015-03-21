(ns git-annex-gallery.core
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as file]
            [clojure.set :as clj-set :refer [difference]]
            [me.raynes.conch :refer [with-programs] :as sh]
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
        get-checksum-config [{ :sh ["git" "annex" "lookupkey" :dir dir] :post str/trim }
                             { :sh ["git" "ls-files" "-s" :dir dir] :post #(second(str/split % #" +")) }
                             { :sh ["sha1sum" path] :post #(first(str/split % #" +")) }]
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

(defn tagify [field-seperator keyvalue-seperator aliases str]
  (let [split-to-fields #(str/split % field-seperator)
        split-key-and-value #(str/split % keyvalue-seperator 2)
        trimmer #(map str/trim %)
        get-key (fn [aliases str]
                  (if (aliases str)
                    (keyword (aliases str))
                    (keyword str)))
        hashify (fn [pair]
                  [(get-key aliases (first pair)) (second pair)])
        ]
  (->> str
      split-to-fields
      (map split-key-and-value)
      (map trimmer)
      (map hashify)
      (into {})
      )))

(defn extract-metadata [path]
  (with-programs [exiv2]
    (tagify #"\n" #":" {"Image timestamp" :timestamp} (exiv2 path))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
