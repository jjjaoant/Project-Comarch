pos1    .fill   1
pos4    .fill   4
neg4    .fill   -4

n       .fill   7
r       .fill   3

combAddr .fill  comb
stAddr  .fill   stack
main    lw      0       5       stAddr
        lw      0       1       n
        lw      0       2       r
        lw      0       4       combAddr
        jalr    4       7
        halt
comb    beq     0       2       base
        beq     1       2       base
        lw      0       6       pos4
        add     6       5       5
        sw      5       7       -4
        sw      5       2       -3
        sw      5       1       -2
        nand    0       0       6
        add     6       1       1
        jalr    4       7
        sw      5       3       -1
        nand    0       0       6
        add     6       2       2
        jalr    4       7
        lw      5       1       -1
        add     1       3       3
        lw      5       1       -2
        lw      5       2       -3
        lw      5       7       -4
        lw      0       6       neg4
        add     6       5       5
        jalr    7       0
base    lw      0       3       pos1
        jalr    7       0
stack   .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
