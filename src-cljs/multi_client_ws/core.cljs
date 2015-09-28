(ns multi-client-ws.core
  (:require [reagent.core :as reagent :refer [atom]]
            [multi-client-ws.websockets :as ws]
            [cognitect.transit :as t]
            [ajax.core :refer [GET]]))

(defonce messages (atom []))
(defonce username (atom nil))
(defonce current-channel (atom "watercooler"))

(def json-reader (t/reader :json))
(def parse-json (partial t/read json-reader))

(defn channel-list []
  [:div.btn-group
   {:role "group"}
   [:br]
   [:button.btn.btn-primary.btn-xs
    {:type "button"
     :value "watercooler"
     :on-click (fn [event] (set-current-channel (aget event "target" "value")))}"Watercooler"]
   [:button.btn.btn-primary.btn-xs
    {:type "button"
     :value "clojure"
     :on-click (fn [event] (set-current-channel (aget event "target" "value")))}"Clojure"]
   [:button.btn.btn-primary.btn-xs
    {:type "button"
     :value "dj_s-wig"
     :on-click (fn [event] (set-current-channel (aget event "target" "value")))}"dj_s-wig"]]
  )

(defn set-current-channel [channel-name]
  (reset! current-channel channel-name)
  (get-initial-messages))

(defn message-list []
  [:ul
   (for [[i message] (map-indexed vector @messages)]
     ^{:key i}
     [:li [:strong (message "username")] ": " (message "message")])])

(defn input-keydown [val key-event]
  (let [key-code (.-keyCode key-event)]
    (when (= key-code 13)
      (ws/send-transit-msg! {:message @val :username @username, :channel @current-channel})
      (reset! val nil))))

(defn message-input []
  (let [value (atom nil)]
    (fn []
      [:input.form-control
       {:type :text
        :placeholder "type in a message and press enter"
        :value @value
        :on-change (fn [event] (reset! value (aget event "target" "value")))
        :on-key-down (partial input-keydown value)
        }])))

(defn name-input []
  (let [value (atom nil)]
    (fn []
      [:span
      [:label "please enter your name:"]
      [:input.form-control
       {:type :text
        :placeholder "Enter your name and press enter"
        :value @value
        :on-change (fn [e] (reset! value (aget e "target" "value")))
        :on-key-down (fn [e] (let [kc (.-keyCode e)]
                               (when (= kc 13)
                                 (reset! username @value)
                                 (println "username is: " @username))
                               ))}]])))
(defn load-more []
  (fn []
    [:input.btn.btn-primary
     {:type :button
      :value "Load More Messages"
      :on-click get-more-messages}]))

(defn input-field []
  (fn []
    (if @username
      [message-input]
      [name-input])))

(defn home-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2 "Welcome to chat, " @username]]]
   [:div.row
    [:div.col-sm-6
     [input-field]]]
   [:div.row
    [:div.col-sm-6
     [channel-list]
     [:h5 "Current Channel: " @current-channel]]]
   [:div.row
    [:div.col-sm-6
     [message-list]]]
   [:div.row
    [:div.col-sm-6
     [load-more]]]])

(defn update-messages! [message]
  (println "updating message: " message)
  (when (= (message "channel") @current-channel)
  (swap! messages #(apply vector (cons message %)))))

(defn mount-components []
  (reagent/render-component [#'home-page] (.getElementById js/document "app")))

(defn get-initial-messages []
  (GET (str "/messages/channel/" @current-channel ) {:handler (fn [response]
                                                                (swap! messages #(parse-json response)))}))
(defn get-more-messages []
  (let [first-msg-id (get (peek @messages) "id")]
    (println first-msg-id)
    (GET (str "/messages/channel/" @current-channel "/message/" first-msg-id) {:handler (fn [response]
                                                                                          (swap! messages #(vec (concat % (parse-json response)))))})))

(defn init! []
  (ws/make-websocket! (str "ws://" (.-host js/location) "/ws") update-messages!)
  (get-initial-messages)
  (mount-components))
