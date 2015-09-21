(ns multi-client-ws.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [multi-client-ws.layout :refer [error-page]]
            [multi-client-ws.routes.home :refer [home-routes]]
            [multi-client-ws.routes.websockets :as wr]
            [multi-client-ws.middleware :as middleware]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [selmer.parser :as parser]
            [environ.core :refer [env]]))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []

  (if (env :dev) (parser/cache-off!))
  (timbre/info (str
                 "\n-=[multi-client-ws started successfully"
                 (when (env :dev) " using the development profile")
                 "]=-")))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "multi-client-ws is shutting down...")
  (timbre/info "shutdown complete!"))

(def app-routes
  (routes
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(def app
  (-> (routes
       wr/websocket-routes
       (wrap-routes home-routes middleware/wrap-csrf))
      middleware/wrap-base))
