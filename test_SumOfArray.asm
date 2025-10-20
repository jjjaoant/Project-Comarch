// SUM OF ARRAY PROGRAM
// Purpose: Calculate sum of array elements
// Algorithm: Linear iteration with pointer
// Input: Array of 5 integers
// Output: Sum in register $3

        // --- SKIP DATA SECTION ---
        beq     0       0       start           // Jump to start (skip array data)

        // --- ARRAY DATA ---
array   .fill   10                              // array[0] = 10 (at address 1)
        .fill   20                              // array[1] = 20 (at address 2)
        .fill   30                              // array[2] = 30 (at address 3)
        .fill   40                              // array[3] = 40 (at address 4)
        .fill   50                              // array[4] = 50 (at address 5)

        // --- INITIALIZATION ---
start   lw      0       1       array_addr      // Load array base address into $1 (pointer = 1)
        add     0       0       3               // Initialize sum accumulator $3 = 0
        lw      0       4       size            // Load array size into $4 (size = 5)
        add     0       0       2               // Initialize index counter $2 = 0

        // --- MAIN SUMMATION LOOP ---
        // Iterate through array: for(i=0; i<size; i++)
loop    lw      1       5       0               // Load array element: $5 = memory[$1 + 0]
        add     3       5       3               // Add to sum: $3 = $3 + $5

        // --- INCREMENT POINTERS ---
        lw      0       6       one             // Load constant 1 into $6
        add     1       6       1               // Increment pointer: $1++ (next address)
        add     2       6       2               // Increment index: $2++ (loop counter)

        // --- CHECK LOOP CONDITION ---
        beq     2       4       done            // If index == size (5), exit loop
        beq     0       0       loop            // Else, continue loop (unconditional jump)

        // --- FINALIZE ---
done    halt                                    // Stop program execution
                                                // Result is in register $3

        // --- CONSTANTS SECTION ---
one         .fill   1                           // Constant: increment value
array_addr  .fill   1                           // Constant: starting address of array
size        .fill   5                           // Constant: number of elements in array
