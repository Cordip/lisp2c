(define (append a b)
  (if (eq? a '()) b
    (cons (car a) (append (cdr a) b))))

(define (hanoi n from to via)
  (if (= n 0) '()
    (append
      (hanoi (- n 1) from via to)
      (cons (cons from (cons to '()))
        (hanoi (- n 1) via to from)))))

(hanoi 3 1 3 2)
