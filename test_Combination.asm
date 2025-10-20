pos1    .fill   1          // Constant: 1
pos4    .fill   4          // Constant: 4 (used for stack frame size)
neg4    .fill   -4         // Constant: -4 (used for stack frame cleanup)

n       .fill   7          // Input n = 7
r       .fill   3          // Input r = 3

combAddr .fill  comb       // Address of the comb function
stAddr  .fill   stack      // Address of the stack base

// --- MAIN ROUTINE ---
main    lw      0       5       stAddr     // R5 = SP (Stack Pointer)
        lw      0       1       n          // R1 = n (7)
        lw      0       2       r          // R2 = r (3)
        lw      0       4       combAddr   // R4 = Address of comb
        jalr    4       7                  // Call comb. R7 = Return Address, PC = R4
        halt                               // Stop execution

// --- COMBINATIONS FUNCTION: comb(n, r) ---
// R1=n, R2=r. Result in R3
comb    beq     0       2       base       // if r == 0, go to base case (result 1)
        beq     1       2       base       // if n == r, go to base case (result 1)

        // Allocate and Save Context
        lw      0       6       pos4       // R6 = 4
        add     6       5       5          // SP = SP + 4 (Allocate frame)
        sw      5       7       -4         // Save RA (R7) @ SP-4
        sw      5       2       -3         // Save r (R2) @ SP-3
        sw      5       1       -2         // Save n (R1) @ SP-2

        // Recursive Call 1: comb(n-1, r)
        nand    0       0       6          // R6 = 0
        add     6       1       1          // R1 = n-1 (n-1 + 0 - 1)
        jalr    4       7                  // Call comb(n-1, r). Result in R3
        sw      5       3       -1         // Save Result 1 (comb(n-1, r)) @ SP-1

        // Recursive Call 2: comb(n-1, r-1)
        nand    0       0       6          // R6 = 0
        add     6       2       2          // R2 = r-1 (r-1 + 0 - 1)
        jalr    4       7                  // Call comb(n-1, r-1). Result in R3

        // Calculate Final Result: Result = R1 + R3
        lw      5       1       -1         // R1 = Result 1 (comb(n-1, r))
        add     1       3       3          // R3 = R1 + R3 (Result 1 + Result 2)

        // Restore Context and Deallocate
        lw      5       1       -2         // R1 = original n
        lw      5       2       -3         // R2 = original r
        lw      5       7       -4         // R7 = original RA
        lw      0       6       neg4       // R6 = -4
        add     6       5       5          // SP = SP - 4 (Deallocate frame)
        jalr    7       0                  // Return to caller (PC = R7)

// --- BASE CASE ---
base    lw      0       3       pos1       // R3 = 1
        jalr    7       0                  // Return to caller (PC = R7)

// --- STACK MEMORY ---
stack   .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
