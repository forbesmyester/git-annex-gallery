(ns git-annex-gallery.core
  (:require [me.raynes.conch :refer [with-programs] :as sh]
            [clojure.java.io :as file]
            [clojure.string :as str])
  (:gen-class))

(defn get-checksum [path]
  (with-programs [sha1sum sed git]
    (let [dir (-> path file/as-file .getParent file/as-file .getPath)
          git-annex-dir (str dir "/" (str/trim (git "-C" dir "rev-parse" "--show-cdup")) ".git/annex")
          ]
      (if (.exists (file/as-file git-annex-dir))
        (git "annex" "lookupkey" path)
        (str/trim (sed "s/ .*//" {:in (sha1sum path)}))
      ))))

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
