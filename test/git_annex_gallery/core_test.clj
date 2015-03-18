(ns git-annex-gallery.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as file]
            [midje.sweet :refer :all]
            [me.raynes.conch :refer [with-programs] :as sh]
            [clojure.string :as str]
            [git-annex-gallery.core :refer :all])
  )

(facts "can list possible markdown documents for a file"
      (fact (get-possible-md "a.png") => #{"a.md" "a.png.md"})
      (fact (get-possible-md "a.edited.png") => #{"a.md" "a.png.md"})
      (fact (get-possible-md "a.tar.gz") => #{"a.tar.md" "a.md" "a.tar.gz.md"})
      (fact (get-possible-md "a.edited.tar.gz") => #{"a.tar.md" "a.md" "a.tar.gz.md"})
      )

(fact "can list source for editeds which should be ignored"
      (list-edited-source ["a.png" "b.png" "b.edited.png"]) => ["b.png"])

(fact "can filter a file list, which will exclude \".md\" descriptions and only show edited"
      (let [files ["a.png" "a.md" "b.md" "c.png" "d.tar.gz" "d.md" "e.png" "e.edited.png"]
            expected ["a.png" "b.md" "c.png" "d.tar.gz" "e.edited.png"]]
            (sort (filter-files files)) => expected))

(fact "can lazily relise vectors and return a pass"
      (do
        (def log (atom []))
        (let [proc (fn [n] (swap! log conj n) (str "s" n))
              check #(= "s3" %)]
          (drop-while-return-checked proc check [1 2 3 4 5 6 7 8]) => { :result "s3" :element 3 :index 2 }
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
