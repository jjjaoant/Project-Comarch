/**
* MULTIPLICATION PROGRAM
* Purpose: Multiply two signed integers
* Algorithm: Shift-and-Add
* Input: mcand (multiplicand), mplier (multiplier)
* Output: Result in register $1
*/

        // INITIALIZATION
        lw      0       1       mcand           // Load multiplicand into $1
        lw      0       2       mplier          // Load multiplier into $2
        add     0       0       7               // Initialize sign counter $7 = 0
        lw      0       6       signBit         // Load sign bit mask (0x80000000) into $6

        // CHECK MULTIPLICAND SIGN
        nand    1       6       3               // $3 = ~(mcand & signBit)
        nand    3       3       3               // $3 = mcand & signBit (check bit 31)
        beq     0       3       chkMp           // If bit 31 = 0 (positive), skip to chkMp
        
        // Multiplicand is negative, convert to positive
        nand    1       1       1               // $1 = ~mcand
        lw      0       3       pos1            // Load constant 1
        add     1       3       1               // $1 = ~mcand + 1 (2's complement = -mcand)
        add     7       3       7               // Increment sign counter (sign++ = 1)

        // CHECK MULTIPLIER SIGN
chkMp   nand    2       6       3               // $3 = ~(mplier & signBit)
        nand    3       3       3               // $3 = mplier & signBit (check bit 31)
        beq     0       3       mult            // If bit 31 = 0 (positive), start multiply
        
        // Multiplier is negative, convert to positive
        nand    2       2       2               // $2 = ~mplier
        lw      0       3       pos1            // Load constant 1
        add     2       3       2               // $2 = ~mplier + 1 (2's complement = -mplier)
        add     7       3       7               // Increment sign counter (sign++ = 1 or 2)

        // MULTIPLICATION SETUP
mult    lw      0       3       pos1            // Initialize bit mask $3 = 1
        lw      0       4       pos16           // Initialize loop counter $4 = 16
        add     0       0       5               // Initialize result accumulator $5 = 0

        // MAIN MULTIPLICATION LOOP
loop    nand    2       3       6               // $6 = ~(mplier & mask)
        nand    6       6       6               // $6 = mplier & mask (check current bit)
        beq     0       6       skip            // If bit = 0, skip addition
        add     5       1       5               // If bit = 1, add mcand to result ($5 += $1)

skip    add     1       1       1               // Shift mcand left (mcand *= 2)
        add     3       3       3               // Shift mask left (mask *= 2, check next bit)
        lw      0       6       neg1            // Load constant -1
        add     4       6       4               // Decrement loop counter ($4--)
        beq     0       4       chkSg           // If counter = 0, done looping
        beq     0       0       loop            // Else, continue loop (unconditional jump)

        // FIX RESULT SIGN
chkSg   lw      0       3       pos1            // Load constant 1
        nand    7       3       6               // $6 = ~(sign_counter & 1)
        nand    6       6       6               // $6 = sign_counter & 1 (check if odd)
        beq     0       6       done            // If even (0 or 2 negatives), result is positive
        nand    5       5       5               // $5 = ~result
        lw      0       3       pos1            // Load constant 1
        add     5       3       5               // $5 = ~result + 1 (2's complement = -result)

        // FINALIZE
done    add     5       0       1               // Move final result from $5 to $1
        halt                                    // Stop program execution

        // DATA SECTION
pos1    .fill   1                               // Constant: positive 1
neg1    .fill   -1                              // Constant: negative 1
pos16   .fill   16                              // Constant: loop count (16 bits)
signBit .fill   -2147483648                     // Constant: sign bit mask (0x80000000)
mcand   .fill   32766                           // Input: multiplicand (test value)
mplier  .fill   10383                           // Input: multiplier (test value)
