(ns multi-client-ws.routes.websockets
  (:require [compojure.core :refer [GET defroutes]]
            [org.httpkit.server
             :refer [send! with-channel on-close on-receive]]
            [taoensso.timbre :as timbre]
            [multi-client-ws.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :as str])
  (import [java.io ByteArrayInputStream]))

(defonce sockets (atom #{}))

(def parse-map {:message 2 :username 4})

(defn parse-input [map-string parse-key] 
  (get (str/split (str/replace map-string #"]" "") #",") (parse-key parse-map)))

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
                           {:username (parse-input % :username) 
                            :message (parse-input % :message)
                            :channel "test-channel"
                            :timestamp (java.util.Date.)})
                          (notify-clients %)))))

(defroutes websocket-routes
  (GET "/ws" request (ws-handler request)))
