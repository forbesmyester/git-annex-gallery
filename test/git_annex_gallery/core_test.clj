(ns git-annex-gallery.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as file]
            [midje.sweet :refer :all]
            [me.raynes.conch :refer [with-programs] :as sh]
            [clojure.string :as str]
            [git-annex-gallery.core :refer :all])
  )

(fact "checksum returns something without spaces - note actually testing a mock!"
      (get-checksum  "./resources/test/images/IMG_20150314_111531.jpg") => "aa933fdc3def24764fd54dc5bd79bd5737fd5a06")

(facts "can tell whether a directory is a leaf"
       ; (fact "non leaf"
       ;   (is-leaf-directory (file/as-file "./resources")) => false)
       (fact "leaf"
         (is-leaf-directory (file/as-file "./resources/albums/ski_trip_2014/slopes")) => true))

(fact "can identify albums"
      (map #(.getPath %) (identify-albums (file/as-file "./resources/albums"))) =>
      ["./resources/albums/a/b/c/d/e"
       "./resources/albums/birthday"
       "./resources/albums/ski_trip_2014/slopes"
       "./resources/albums/ski_trip_2014/chalet"])

(fact "Can extract data from a exiv2 like data structure"
      (tagify #"\n" #":" {"how are" :howare} "hi: there\nhow are: you") =>
      {:hi "there" :howare "you"})

(fact "Can extract metadata from an image"
      (extract-metadata "./resources/test/images/IMG_20150314_111531.jpg")
      => (contains {:timestamp "2015:03:14 11:15:32"}))
