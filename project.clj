(defproject git-annex-gallery "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 ]
  :main ^:skip-aot git-annex-gallery.core
  :user {:plugins [[lein-midje "3.1.3"]]}
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}
                   :uberjar {:aot :all}})
