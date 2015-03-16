(ns git-annex-gallery.core
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as file]
            [clojure.set :as clj-set :refer [difference]]
            [me.raynes.conch :refer [with-programs] :as sh]
            [clojure.string :as str])
  (:gen-class))

(defn get-possible-md [fn]
  (if (re-find #"\.md$" fn)
    []
    (loop [myfn fn
           result []]
      (let [next-fn (str/replace myfn #"\.[^\.]*$" "")]
        (if (= (.indexOf myfn ".") -1)
          result
          (recur next-fn (conj result (str next-fn ".md"))))))))

(defn filter-files [files]
  (let [filter-out (set (flatten (map get-possible-md files)))]
    (difference (set files) filter-out)
    ))

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
  (filter
    #(and (.isDirectory %) (is-leaf-directory %))
    (file-seq root-path)))

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
