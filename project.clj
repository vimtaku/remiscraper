(defproject remiscraper "0.1.0-remiscraper"
  :description "get all recipe name from ryorisapuri.jp and check google search result"
  :url "http://github.com/vimtaku/remiscraper"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main remiscraper.core
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [enlive "1.1.5"]
                 [clj-http "1.0.0"]
                 [org.clojure/data.json "0.2.5"]
                 ])
