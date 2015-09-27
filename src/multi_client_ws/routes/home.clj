(ns multi-client-ws.routes.home
  (:require [multi-client-ws.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]
            [multi-client-ws.db.core :as db]
            [clojure.data.json :as json]))

(defn home-page []
  (layout/render "home.html"))

(defn date-converter [key value]
  (if (= key :timestamp)
    (str value)
    value))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/docs" [] (ok (-> "docs/docs.md" io/resource slurp)))
  (GET "/messages/:channel" [channel] {:status "200"
                                       :header {}
                                       :body (json/write-str (db/get-messages-by-channel {:channel channel}) :value-fn date-converter)}))
