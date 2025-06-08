set xlabel "Anzahl Threads"
set ylabel "Frames / Sek."
set key at 5, 4.5
set xtics(3,6,8,9,12,15,18)
set ytics (1,2,3,4)
set arrow from 8,0 to 8,5 nohead lc rgb 'black' dt 2
plot [0:19][0:5] "messung_local.txt" title "Ausfuehrung lokal" with linespoints pointtype 5, "messung_netzw.txt" title "Ausfuehrung im LAN" with linespoints pointtype 7