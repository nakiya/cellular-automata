(ns automata.core
  (:require
   [automata.config :as config]
   [automata.automata :as auto]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (auto/run))

(defn ^:export init []
  (dev-setup)
  (mount-root))
