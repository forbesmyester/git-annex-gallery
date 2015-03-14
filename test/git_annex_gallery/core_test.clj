(ns git-annex-gallery.core-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [git-annex-gallery.core :refer :all]))

(fact "Can extract data from a exiv2 like data structure"
      (tagify #"\n" #":" {"how are" :howare} "hi: there\nhow are: you") =>
      {:hi "there" :howare "you"})

(fact "Can extract metadata from an image"
      (extract-metadata "./resources/test/images/IMG_20150314_111531.jpg")
      => (contains {:timestamp "2015:03:14 11:15:32"}))
