(ns bus-routes.util)


(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))
