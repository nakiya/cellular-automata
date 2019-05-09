(ns automata.automata)

(defn get-adjacent-cells [[x y]]
  (for [dx '(-1 0 1) dy '(-1 0 1) :when (or (not= dx 0) (not= dy 0))]
    [(+ x dx) (+ y dy)]))

(defn live-area-cells [cells]
  "Figure out hot cells around live cells"
  (let [lc (transient #{})]
    (doseq [i cells]
      (do (doseq [c (get-adjacent-cells i)]
            (conj! lc c))
          (conj! lc i)))
    (persistent! lc)))

(defn next-gen [cells]
  "Create next generation. Only consider cells around live cells."
  (let [lac (live-area-cells cells)
        ng (transient #{})]
    (doseq [cell lac]
      (let [live-count (->> (get-adjacent-cells cell)
                            (filter cells)
                            (count))]
        (if (cells cell)
          ;;Cell is already alive
          (when-not (or (> live-count 3) (< live-count 2))
            (conj! ng cell))
          (when (= live-count 3)
            (conj! ng cell)))))
    (persistent! ng)))

;; Below are drawing functions

(def cell-size 4)
(def grid-width 200)
(def grid-height 200)

(defn center-pattern [pattern]
  (->> pattern
       (map (fn [[x y]] [(+ (quot grid-width 2) x) (+ (quot grid-height 2) y)]))
       (into #{})))

(def r-pentomino [[0 0] [1 0] [0 1] [-1 1] [0 2]])
(def diehard [[1 2] [2 2] [2 3] [6 3] [7 1] [7 3] [8 3]])
(def acorn [[1 3] [2 1] [2 3] [4 2] [5 3] [6 3] [7 3]])

;; (def world (atom (center-pattern r-pentomino)))
;; (def world (atom (center-pattern diehard)))
(def world (atom (center-pattern acorn)))

(defn draw-cell [ctx [x y]]
  (set! (.-fillStyle ctx) "black")
  (.fillRect ctx (* cell-size x) (* cell-size y) cell-size cell-size))

(defn draw-cells [ctx cells]
  (doseq [i cells]
    (draw-cell ctx i)))

(defn draw-line [ctx x1 y1 x2 y2]
  (set! (.-lineWidth ctx) 0.4)
  (.beginPath ctx)
  (.moveTo ctx x1 y1)
  (.lineTo ctx x2 y2)
  (.stroke ctx))

(defn draw-gridlines [ctx]
  (let [max-width (* cell-size grid-width)
        max-height (* cell-size grid-height)]
    (dotimes [i grid-width]
      (draw-line ctx 0 (* cell-size i) max-width (* cell-size i))
      (draw-line ctx (* cell-size i) 0 (* cell-size i) max-height))))

(defn setup-grid [canvas]
  (set! (.-width canvas) (* cell-size grid-width))
  (set! (.-height canvas) (* cell-size grid-height)))

(defn clear-canvas [ctx]
  (.clearRect ctx 0 0 (.-width ctx) (.-height ctx)))

(defn sleep [f ms]
  (js/setTimeout f ms))

(defn draw-gen []
  (let [canvas (.getElementById js/document "world")
        ctx (.getContext canvas "2d")]
    (clear-canvas ctx)
    (setup-grid canvas)
    ;; (draw-gridlines ctx)
    (draw-cells ctx @world)
    (swap! world next-gen)
    (sleep draw-gen 100)))

(defn ^:export run []
  (println "automata/run")
  (draw-gen))
