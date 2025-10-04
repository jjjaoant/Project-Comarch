    lw 0 1 num        load 10 into reg1
    beq 1 1 skip      since reg1==reg1 → jump to skip
    halt              should be skipped
skip halt
num .fill 10
