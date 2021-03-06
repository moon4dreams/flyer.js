(ns flyer.window
  (:require [flyer.storage :as storage]
            [flyer.utils :as utils]
            [goog.events :as events]))

(def this-window js/window)

(def external-window-list
  "based on the current window, it will store references to any
external windows that are opened using the 'open' function"
  (atom {}))

(defn gen-window-options [& options]
  (if (even? (count options))
    (let [options-twos
          (loop [options options
                 options-vonc []]
            (if (not (empty? options))
              (recur (rest (rest options))
                     (conj options-vonc
                           (map #(if (keyword? %) (name %) %)
                                (take 2 options))))
              options-vonc))
          options-inter
          (map #(apply str (interpose "=" %)) options-twos)]
      (apply str (interpose ", " options-inter)))
    (.error js/console "options needs an even number of terms")))

(defn ^:export open [url name & options]
  (let [options-str (cond
                     (and (= (count options) 1)
                          (= (type (first options)) js/String))
                     (first options)
                     :else
                     (apply gen-window-options options))
        window (.open this-window url name options-str)]
    (storage/insert-window-ref! window)
    (events/listen
     window (.-BEFOREUNLOAD events/EventType)
     (fn [event]
       (storage/remove-window-ref! window)
       nil))
    window))

(defn register-external
  ([window] (storage/insert-window-ref! window))
  ([] (register-external js/window)))

