(ns git-annex-gallery.core
  (:require [me.raynes.conch :refer [let-programs] :as sh]
            [clojure.string :as str])
  (:gen-class))

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
    (tagify #"\n" #":" {"Image timestamp" :timestamp} (exiv2 path)))
  )


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
