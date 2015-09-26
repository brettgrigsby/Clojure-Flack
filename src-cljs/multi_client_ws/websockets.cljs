(ns multi-client-ws.websockets
  (:require [cognitect.transit :as t]))

(defonce ws-chan (atom nil))
(def json-reader (t/reader :json))
(def json-writer (t/writer :json))
(def parse-json (partial t/read json-reader))

(defn clj->json
 [ds]
 (.stringify js/JSON (clj->js ds)))

(defn with-parsing
  [update-fn]
  (fn [event]
    (-> event
        .-data
        parse-json
        update-fn)))

(defn with-callback! [chan callback]
  (let [cb (with-parsing callback)]
    (set! (.-onmessage chan) cb)
    chan))

(defn send-transit-msg!
  [msg]
  (if @ws-chan
    (.send @ws-chan (clj->json msg))
    (throw (js/Error. "Websocket not available!"))))

(defn make-websocket! [url receive-handler]
  (println "attempting to connect to websocket")
  (if-let [chan (js/WebSocket. url)]
    (reset! ws-chan (with-callback! chan receive-handler))
    (throw (js/Error. "Websocket connection failed!"))))
