(ns multi-client-ws.routes.websockets
  (:require [compojure.core :refer [GET defroutes]]
            [org.httpkit.server
             :refer [send! with-channel on-close on-receive]]
            [cognitect.transit :as t]
            [taoensso.timbre :as timbre]))

(defonce channels (atom #{}))

(defn connect! [channel]
  (timbre/info "channel open")
  (swap! channels conj channel))

(defn disconnect! [channel status]
  (timbre/info "channel closed:" status)
  (swap! channels (fn [cs] (remove #{channel} cs))))

(defn notify-clients [msg]
  (doseq [channel @channels]
    (send! channel msg)))

(defn ws-handler [request]
  (with-channel request channel
    (connect! channel)
    (on-close channel (partial disconnect! channel))
    (on-receive channel #(notify-clients %))))

(defroutes websocket-routes
  (GET "/ws" request (ws-handler request)))
