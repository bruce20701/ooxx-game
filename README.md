# OOXX遊戲(ooxx-game)
功能介紹:<br>
這個程式可以在本地或是連線與他人玩OOXX，在9X9的棋盤中先連成五個一線的一方就獲勝。<br><br>
關鍵技術:<br>
1.GUI，GUI是用來顯示遊戲畫面與各種選單畫面<br>
2.動畫，動畫最主要用到的地方是當有人獲勝時畫出連線，避免一獲勝就直接跳到獲勝畫面，讓人摸不著頭緒<br>
3.Socket，Socket是用來與其他人連線溝通的<br>
4.多執行緒，使用多執行緒是為了當與其他電腦溝通時GUI畫面不會卡住<br><br>
演示影片:https://www.youtube.com/watch?v=NW5jCvJaXXg
