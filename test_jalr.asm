    lw 0 1 addr       load address of subroutine into reg1
    jalr 1 2          jump to subroutine, save return address in reg2
    halt
sub add 0 0 3         reg3 = 0
    halt
addr .fill sub
