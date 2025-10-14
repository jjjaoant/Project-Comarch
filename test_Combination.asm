beq     0       0       main    branch to main
        noop                            n (reg 1)
        noop                            r (reg 2)
        noop                            return value (reg 3)
        noop                            temp (reg 4)
        noop                            stack pointer (reg 5)
        noop                            temp (reg 6)
        noop                            return address (reg 7)
pos1    .fill   1
pos4    .fill   4
neg4    .fill   -4
combAddr .fill  comb
n       .fill   7
r       .fill   3
main    lw      0       5       stAddr  load stack address
        lw      0       1       n       load n
        lw      0       2       r       load r
        lw      0       4       combAddr load comb address
        jalr    4       7       call combination
        halt
comb    beq     0       2       base    if r == 0, base case
        beq     1       2       base    if n == r, base case
        lw      0       4       combAddr load comb address
        lw      0       6       pos4    load 4
        add     6       5       5       SP += 4
        sw      5       7       -4      store return address at SP-4
        sw      5       2       -3      store r at SP-3
        sw      5       1       -2      store n at SP-2
        nand    0       0       6       x6 = -1
        add     6       1       1       n = n - 1
        jalr    4       7       call comb(n-1, r)
        sw      5       3       -1      store result at SP-1
        nand    0       0       6       x6 = -1
        add     6       2       2       r = r - 1
        jalr    4       7       call comb(n-1, r-1)
        lw      5       1       -1      load first result
        add     1       3       3       add results
        lw      5       1       -2      restore n
        lw      5       2       -3      restore r
        lw      5       7       -4      restore return address
        lw      0       6       neg4    load -4
        add     6       5       5       SP -= 4
        jalr    7       0       return
base    lw      0       3       pos1    return 1
        jalr    7       0       return
stack   noop
stAddr  .fill   stack
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0