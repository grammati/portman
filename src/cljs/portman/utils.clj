(ns portman.utils)

(defmacro for! [& body]
  `(doall (for ~@body)))


(defmacro cur-for [[sym cur & more] & body]
  `(let [cur# ~cur]
     (doall
      (for [i#   (range (count @cur#))
            :let [~sym (reagent.core/cursor cur# [i#])]
            ~@more]
        ~@body))))
