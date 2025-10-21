lw      0       1       mcand   load multiplicand
        lw      0       2       mplier  load multiplier
        add     0       0       7       $7 = 0 (sign counter)
        lw      0       6       sBit $6 = 0x80000000
        nand    1       6       3       check mcand sign
        nand    3       3       3
        beq     0       3       chkMp   if 0, mcand >= 0
        nand    1       1       1       negate mcand
        lw      0       3       pos1
        add     1       3       1
        add     7       3       7       sign++
chkMp   nand    2       6       3       check mplier sign
        nand    3       3       3
        beq     0       3       mult    if 0, mplier >= 0
        nand    2       2       2       negate mplier
        lw      0       3       pos1
        add     2       3       2
        add     7       3       7       sign++
mult    lw      0       3       pos1    start multiply
        lw      0       4       pos16
        add     0       0       5
loop    nand    2       3       6
        nand    6       6       6
        beq     0       6       skip
        add     5       1       5
skip    add     1       1       1
        add     3       3       3
        lw      0       6       neg1
        add     4       6       4
        beq     0       4       chkSg
        beq     0       0       loop
chkSg   lw      0       3       pos1
        nand    7       3       6
        nand    6       6       6
        beq     0       6       done
        nand    5       5       5
        lw      0       3       pos1
        add     5       3       5
done    add     5       0       1
        halt
pos1    .fill   1
neg1    .fill   -1
pos16   .fill   16
sBit .fill   -2147483648
mcand   .fill   32766
mplier  .fill   10383
