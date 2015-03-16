(ns git-annex-gallery.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as file]
            [midje.sweet :refer :all]
            [me.raynes.conch :refer [with-programs] :as sh]
            [clojure.string :as str]
            [git-annex-gallery.core :refer :all])
  )

(fact "can lazily relise vectors and return a pass"
      (do
        (def log (atom []))
        (let [proc (fn [n] (swap! log conj n) (str "s" n))
              check #(= "s3" %)
              post #(str "result: " %1 " " %2)]
          (drop-while-return-checked proc check post [1 2 3 4 5 6 7 8]) => "result: s3 3"
          @log => [1 2 3]
          )))

(fact "checksum returns something without spaces"
      (get-checksum  "./resources/test/images/IMG_20150314_111531.jpg") => "dc903a138487161e512d3b494fae29c619cc2666")

(facts "can tell whether a directory is a leaf"
       (fact "leaf"
         (is-leaf-directory (file/as-file "./resources/albums/ski_trip_2014/slopes")) => true))

(fact "can identify albums"
      (sort (map #(.getPath %) (identify-albums (file/as-file "./resources/albums")))) =>
      (sort ["./resources/albums/a/b/c/d/e" "./resources/albums/birthday" "./resources/albums/ski_trip_2014/slopes" "./resources/albums/ski_trip_2014/chalet"]))

(fact "Can extract data from a exiv2 like data structure"
      (tagify #"\n" #":" {"how are" :howare} "hi: there\nhow are: you") =>
      {:hi "there" :howare "you"})

(fact "Can extract metadata from an image"
      (extract-metadata "./resources/test/images/IMG_20150314_111531.jpg")
      => (contains {:timestamp "2015:03:14 11:15:32"}))
