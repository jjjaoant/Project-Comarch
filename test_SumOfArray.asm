        beq     0       0       start
array   .fill   5      ; mem[1]
        .fill   1      ; mem[2]
        .fill   1      ; mem[3]
        .fill   1      ; mem[4]
        .fill   1      ; mem[5]

start   lw      0       1       Aaddr  ; R1 = 1 (address ของ array[0])
        add     0       0       3           ; R3 = 0 (sum)
        lw      0       4       size        ; R4 = 5
        add     0       0       2           ; R2 = 0 (index)

loop    lw      1       5       0           ; R5 = mem[R1] (โหลด array element)
        add     3       5       3           ; R3 += R5

        lw      0       6       one         ; R6 = 1
        add     1       6       1           ; R1++ (ชี้ไป element ถัดไป)
        add     2       6       2           ; R2++ (index++)

        beq     2       4       done        ; if index == size, done
        beq     0       0       loop        ; continue

done    halt
one         .fill   1
Aaddr  .fill   1       ; address ของ array[0] = 1
size        .fill   5
