.fill       32766            ; mcand = 32766 (ตัวเลขที่เราจะคูณ)
.fill       10383            ; mplier = 10383 (ตัวเลขที่ใช้คูณ)
.fill       0                ; ผลลัพธ์จะเก็บที่นี่
.fill       1                ; ใช้สำหรับการทำ loop

mcand   .fill       32766            ; กำหนดค่า mcand
mplier  .fill       10383            ; กำหนดค่า mplier

        lw          0       1       mcand          ; โหลดค่า mcand ลงใน reg1
        lw          0       2       mplier         ; โหลดค่า mplier ลงใน reg2
        lw          0       3       zero           ; โหลดค่า 0 ลงใน reg3 สำหรับเก็บผลลัพธ์
        lw          0       4       one            ; โหลดค่า 1 ลงใน reg4 สำหรับ loop counter

loop    beq         2       0       end_loop       ; ถ้า mplier == 0 ให้หยุด
        and         5       2       2              ; เช็คว่า mplier เป็นเลขคี่หรือไม่
        beq         5       0       next_step       ; ถ้าไม่ใช่เลขคี่ให้ข้ามไป
        add         3       1       3              ; ถ้า mplier เป็นเลขคี่ ให้เพิ่ม mcand ลงในผลลัพธ์ (reg3)

next_step
        add         1       1       1              ; คูณ mcand ด้วย 2 (shift left 1 bit)
        add         2       2       2              ; ลด mplier ลงครึ่งหนึ่ง
        beq         0       0       loop            ; ทำซ้ำคำสั่ง

end_loop
        sw          0       3       result          ; เก็บผลลัพธ์ที่ reg3 ลงใน memory (ใน result)
        halt

zero    .fill       0                ; ค่าศูนย์
one     .fill       1                ; ค่าหนึ่ง
result  .fill       0                ; Memory location สำหรับเก็บผลลัพธ์
