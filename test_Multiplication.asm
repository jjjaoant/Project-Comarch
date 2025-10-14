lw      0       1       mcand   load multiplicand into $1
        lw      0       2       mplier  load multiplier into $2
        lw      0       3       pos1    $3 = 1 (for bit mask)
        lw      0       4       pos16   $4 = 16 (bit counter, check 16 times)
        add     0       0       5       $5 = 0 (result accumulator)
loop    nand    2       3       6       $6 = ~(mplier & mask)
        nand    6       6       6       $6 = mplier & mask
        beq     0       6       skip    if bit is 0, skip addition
        add     5       1       5       result += mcand
skip    add     1       1       1       mcand = mcand << 1 (multiply by 2)
        add     3       3       3       mask = mask << 1
        lw      0       6       neg1    $6 = -1
        add     4       6       4       counter--
        beq     0       4       done    if counter == 0, done
        beq     0       0       loop    continue loop
done    add     5       0       1       move result to $1
        halt
pos1    .fill   1
neg1    .fill   -1
pos16   .fill   16
mcand   .fill   32766
mplier  .fill   10383