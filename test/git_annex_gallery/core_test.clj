(ns git-annex-gallery.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as file]
            [clojure.java.shell :as shell]
            [midje.sweet :refer :all]
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
      (identify-albums (file/as-file "./resources/albums")) =>
      (just ["./resources/albums/a/b/c/d/e" "./resources/albums/birthday" "./resources/albums/ski_trip_2014/slopes" "./resources/albums/ski_trip_2014/chalet"]))

(fact "can list images"
      (identify-files (file/as-file "./resources/albums/ski_trip_2014/slopes")) =>
            (just [
             "./resources/albums/ski_trip_2014/slopes/iDark-Sunset-and-Red-Sun-Rays_Winter__11868-150x150.jpg"
             "./resources/albums/ski_trip_2014/slopes/iSnowy-Trees_Winter-in-the-Park__94185-150x150.jpg"
             "./resources/albums/ski_trip_2014/slopes/iWooden-benches-under-the-snow__52247-150x150.jpg"
             ]))

(fact "mega-mapper can pull out and process"
      (mega-mapper
        {
         ["a" "b"] (fn [ks vs] { "AB" (str (apply str ks) "-" (apply str vs)) })
         ["c"] (fn [ks vs] {"X" 9} )
         }
        { "a" 1 "b" 2 "c" 3 }
        ) => { "X" 9 "a" 1 "b" 2 "c" 3 "AB" "ab-12" })

(fact "Can extract data from a exiv2 like data structure"
      (tagify
        #"\n"
        #":"
        { ["how are"] (fn [ks vs] {:howare (first vs)}) }
        "hi: there\nhow are: you") => {"hi" "there" "how are" "you" :howare "you"})

(fact "Can extract metadata from an image"
      (extract-metadata "./resources/test/images/IMG_20150314_111531.jpg")
      => (contains {:timestamp "2015:03:14 11:15:32"
                    :width 2448
                    :height 3264}))

(facts "Can remove directories"
      (fact "get to consistent state" (remove-cache ".thumbs") => true)
      (fact "should return true when no dir exists" (remove-cache ".thumbs") => true)
      (fact "create a thumb directory to test actual deletion" (:exit (shell/sh "mkdir" "-p" ".thumbs/some/sub/directories")) => 0)
      (fact "should return true when dir exists" (remove-cache ".thumbs") => true)
      )

(facts "can generate a thumbnail but will also not recreate when already existing"
      (do
      (remove-cache ".thumbs")
      (fact (get-thumbnail "160x120" "png" ".thumbs"  "./resources/test/images/IMG_20150314_111531.jpg") => [".thumbs/160x120.png" 1])
      (fact (get-thumbnail "160x120" "png" ".thumbs"  "./resources/test/images/IMG_20150314_111531.jpg") => [".thumbs/160x120.png" 0])
      ))

(fact "can generate thumbnails"
      (do
        (remove-cache ".thumbs")
        (get-thumbnails [[320 240] [640 480]] "png" ".thumbs" "./resources/test/images/IMG_20150314_111531.jpg") =>
        [".thumbs/dc903a138487161e512d3b494fae29c619cc2666/320x240.png"
         ".thumbs/dc903a138487161e512d3b494fae29c619cc2666/640x480.png"]))


