(ns git-annex-gallery.core
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as file]
            [me.raynes.conch :refer [with-programs] :as sh]
            [clojure.string :as str])
  (:gen-class))

(defn drop-while-return-checked
  "
  Runs proc on items in data and returns a passing item, without evaluating anything else.

  Parameters
   * Function proc [element] A function to run on an elements of data
   * Function check [result-of-proc] The check that will be performe don the output of proc
   * Function post [result-of-proc element] If result-of-proc passed `check` this, the result of this function will be returned.
  "
  ([proc check post data n]
   (if (< n (count data))
     (let [r (proc (nth data n))]
       (if (check r) (post r (nth data n)) (drop-while-return-checked proc check post data (inc n))))))
  ([proc check post data] (drop-while-return-checked proc check post data 0)))

(defn get-checksum [path]
  (let [dir (-> path file/as-file .getParent file/as-file .getPath)
        filename (-> path file/as-file .getName)
        get-checksum-config [{ :sh ["git" "annex" "lookupkey" :dir dir] :post str/trim }
                             { :sh ["git" "ls-files" "-s" :dir dir] :post #(second(str/split % #" +")) }
                             { :sh ["sha1sum" path] :post #(first(str/split % #" +")) }]
        ]
    (drop-while-return-checked
        #(apply shell/sh (:sh %))
        #(= 0 (:exit %))
        #((:post %2) (:out %1))
        get-checksum-config
    )))

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
