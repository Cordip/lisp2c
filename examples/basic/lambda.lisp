(define (make-adder n) (lambda (x) (+ n x))) (define add5 (make-adder 5)) (add5 3)
