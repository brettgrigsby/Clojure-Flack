(ns multi-client-ws.routes.websockets
  (:require [compojure.core :refer [GET defroutes]]
            [org.httpkit.server
             :refer [send! with-channel on-close on-receive]]
            [taoensso.timbre :as timbre]
            [multi-client-ws.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :as str]
            [clojure.data.json :as json]))

(defonce sockets (atom #{}))

(defn connect! [socket]
  (timbre/info "channel open")
  (swap! sockets conj socket))

(defn disconnect! [socket status]
  (timbre/info "channel closed:" status)
  (swap! sockets (fn [cs] (remove #{socket} cs))))

(defn notify-clients [msg]
  (doseq [socket @sockets]
    (send! socket msg)))

(defn ws-handler [request]
  (with-channel request socket
    (connect! socket)
    (on-close socket (partial disconnect! socket))
    (on-receive socket #(do
                          (db/save-message!
                           {:username ((json/read-str %) "username")
                            :message ((json/read-str %) "message")
                            :channel "home"
                            :timestamp (java.util.Date.)})
                          (notify-clients %)))))

(defroutes websocket-routes
  (GET "/ws" request (ws-handler request)))
