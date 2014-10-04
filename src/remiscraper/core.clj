(ns remiscraper.core
  (:use [clojure.java.io])
  (:require
    [net.cgrand.enlive-html :as e]
    [clj-http.client :as client]
    [clojure.data.json :as json]
  )
)


(defn write-result [s path]
  "Write s to path"
  (with-open [wrtr (writer path :append true)]
    (.write wrtr s)
  )
)

(def remi "https://ryorisapuri.jp")
(def ingredients-page (str remi "/search/ingredients"))
(def chefs-page (str remi "/chefs"))

(defn get-categories-uris []
  (let [ingredients-html (e/html-resource (reader ingredients-page))]
    (filter (fn [u] (re-find #"\?" u)) (distinct
      (map #(str remi %)
        (map #(get-in % [:attrs :href])
          (e/select ingredients-html [:div.list-group :a])
        )
      )
    ))
  )
)

(defn get-chefs-uris []
  (let [chefs-html (e/html-resource (reader chefs-page))]
    (map #(str remi %)
      (filter (fn [u] (re-find #"chefs/" u))
        (map #(get-in % [:attrs :href]) (e/select chefs-html [:a]))
      )
    )
  )
)

(defn add-offset [url offset]
  (if (re-find #"\?" url)
    (str url "&offset=" offset)
    (str url "?offset=" offset)
  )
)

(defn get-json [url]
  (client/get url
    {:headers
     {:X-Requested-With "XMLHttpRequest"
      :Accept "application/json, text/javascript, */*; q=0.01"
      :Accept-Encoding "gzip, deflate"
      :Accept-Language "ja,en-us;q=0.7,en;q=0.3"
      :User-Agent "remiscraper"
     }})
)
(defn get-json-with-sleep [url ms]
  (Thread/sleep ms)
  (get-json url)
)
(defn res-logger [res]
  (write-result res "res.log")
)

(defn sleeptime []
  (+ 2000 (rand-int 3000))
)
(defn get-recipe-title [c-url offset]
  (let [
         c-json (get-json-with-sleep (add-offset c-url offset) (sleeptime))
         body-json (json/read-str (:body c-json))
         has_next? (get-in body-json ["count_info" "has_next"])
         next-offset (get-in body-json ["count_info" "offset"])
         result (fn [] (map #(% "title") (body-json "recipes")))
       ]

       ;(res-logger (str c-json))

       (if has_next?
         (concat (result) (get-recipe-title c-url next-offset))
         (result)
       )
  )
)


(defn scrape-all-recipe-title [dest]
  (defn scrape-and-write [u]
    (write-result
      (str (clojure.string/join "\n" (get-recipe-title u 0)) "\n")
    dest)
  )

  (doall (map scrape-and-write (get-chefs-uris)))
)
;(scrape-all-recipe-title "/tmp/result.txt")


(defn -main [& args]
  (doall (map println (get-recipe-title (first (get-chefs-uris)) 0) ))
)

