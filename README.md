# Scan-RDP-Server
Scan IP Range to Find RDP Server...


Just fun...

집에 원격데스크탑을 켜놓고 다니는데...
아이피가 먼지 모르겠다. ㅋㅋ

원래 빠른유선을 DHCP로 Real-IP를 잡고...
보조로 Wifi를 잡아둔다.

아무데나 가서 Wifi로 먼저 원격로그인을 하고, 유선에 할당된 Real-IP를 체크한 후...
유선으로 접속한다.

유선의 Real-Ip는 잘 바뀌진 않아, 적어놓는데... 가끔 바뀐다. 
전기세 아까워 안쓸때 절전으로 해놓고... 출근전 켜서 사용한다.

가끔씩, 노트북이 유선망 잡히고나서, Wifi를 안잡는 경우가 생긴다. 
자기딴엔 인터넷이 되는걸 확인했다고 생각하는지, 항상연결로 설정한 Wifi 연결을 주인허락없이 연결안할때가 있다. 
윈도우10 !!! 염병.... ㅋㅋ

그럼 난 집에 노트북에 접근할 방법이 없고 ㅋㅋ
Wifi만 공유기의 DDNS 설정이 잡혀있어서 난감했지. ㅎ

이전에 적어둔 Real-IP로도 당근 접근 안되고...

그래서 RDP 접근 체크를 만들어 봤다.
적어둔 Real-IP의 B클래스를 기반으로 하위 전체 아이피를 풀-스캔 !!! ㅎㅎ

잘된다.

나처럼 RDP 쓰는 사람이 많은지 97개나 찾아지네 ㅎ 물론 3389포트만 검색 ㅎ


대부분의 클라이언트는 연결요청하고, 특정한 값(서로 약속된)을 보내고... ---- 서버는 그에 특정한 값으로 응답한다...
대부분 예전만들어진것들이라...

자바 프로그램으로 서버소켓 열고.... 원격데스크탑 프로그램으로 해당 포트 연결하면 클라이언트가 던지는 특정한 값을 받을 수 있고!!!
클라이언트 소켓을 열어서... 또다른 진짜 RDP서버에 접근해서 위에서 얻어진 값을 연결과 동시에 던져주면, 서버에서 응답해주는 특정한 값을 받을 수 있다!!!

따라서 
1000개정도 동시 Thread로 지정된 IP범위에 연결하면서 데이터를 던지고.... 서버에서 보내는 값이 오는지 체크하면 ! 끝!!!

이렇게 노는 것도 잼있네... 쩝.
일은 바쁜데 별짓을 다하는 ㅋㅋ

여러모로 응용할 수 있겠지만, 
필요할때 잠깐 쓸정도... 여기까지만 ㅎㅎㅎ

와이프에게 쿠사리 한방 안먹었어도 뻘짓 안했을텐데 ㅋㅋ
