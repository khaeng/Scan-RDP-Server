import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 
 */

/**
 * @author khaeng@nate.com
 *
 */
public class ScanRdpServer {

	private static final int THREAD_COUNT = 1000;
	private static final int CONN_TIMEOUT = 2*1000;
	private static final int READ_TIMEOUT = 2*1000;
	private static final String RDP_CHECK_SENDING_HEX_STRING 	= "30000130ee00000000000010008000b000000"; // 둘다 RDP 요청값으로 정상 수행됨.
	// private static final String RDP_CHECK_SENDING_HEX_STRING 	= "300002b26e00000000000436f6f6b69653a206d737473686173683d6861656e670d0a010008000b000000";
	private static final String RDP_RETURN_CONN_HEX_STRING 	= "30000130ed00000123400021f080008000000";
	
	private static boolean isFouncRdpSvr = false;
	private static Executor executor = Executors.newFixedThreadPool(THREAD_COUNT); // 1000개씩 체크한다.
	private static int checkCount = 0;
	private static StringBuffer foundRdpInfo = new StringBuffer();
	private static int foundRdpSvrCount = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// System.out.println(new CheckIIpPort().getInfoIpPortConn("211.110.219.107", 3389, "30000130ee00000000000010008000b000000"));
// 		System.out.println(new CheckIIpPort().getInfoIpPortConn("211.110.219.107", 3389, "300002b26e00000000000436f6f6b69653a206d737473686173683d6861656e670d0a010008000b000000", false));
		// new CheckIIpPort().getClientInfo(3388);
		boolean isStopAtFound = false;
		int rdpPort = 3389;
		String ipBClassRange = "211.110.";
		int rangeBStart = 1;
		int rangeBEnd = 254;
		long startTime = System.currentTimeMillis();
		
		executor.execute(() -> {
			while (!isFouncRdpSvr && checkCount < (rangeBEnd-rangeBStart+1) * 254) {
				try {
					if(checkCount%100==0) {
						Thread.sleep(5000);
						System.out.println("Checking count : " + checkCount);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(3000);
				System.out.println("Check Count : " + checkCount);
				System.out.println("=======================[FoundResult] ::: " + foundRdpSvrCount + " Founded");
				System.out.println(foundRdpInfo.toString());
				System.out.println("=======================[FoundResult] ::: " + foundRdpSvrCount + " Founded");
				System.out.println("Check Count : " + checkCount);
				System.out.println(String.format("IP Range : %s ~ %s\nSearch Running Time : %,dms\nFounded Count : %,dea"
						, ipBClassRange + rangeBStart + ".1"
						, ipBClassRange + rangeBEnd + ".254"
						, System.currentTimeMillis() - startTime
						, foundRdpSvrCount));
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
		});
		
		for (int i = rangeBStart; i <= rangeBEnd; i++) {
			final String checkTargetIpaddress = ipBClassRange+i+".";
			checkIpCClassRange(checkTargetIpaddress, rdpPort, 1, 254, isStopAtFound);
		}
	}

	private static void checkIpCClassRange(String ipCClassRange, int port, int rangeStart, int rangeEnd, boolean isStopFound) {
		for (int i = rangeStart; i <= rangeEnd; i++) {
			final String checkTargetIpaddress = ipCClassRange+i;
			executor.execute(()->{
				new ScanRdpServer().getInfoIpPortConn(checkTargetIpaddress, port, RDP_CHECK_SENDING_HEX_STRING, isStopFound);
				synchronized (executor) {
					checkCount++;
				}
			});
		}
	}

	public void getClientInfo(int port) {
		try(ServerSocket serverSocket = new ServerSocket(port)){
			serverSocket.setReuseAddress(true);
			serverSocket.setSoTimeout(READ_TIMEOUT);
			// serverSocket.bind(new InetSocketAddress(port));
			while (true) {
				if(!serverSocket.isBound() || serverSocket.isClosed())
					break;
				try(Socket socket = serverSocket.accept()) {
					int readTimeout = READ_TIMEOUT;
					long readStartTime = System.currentTimeMillis(); // 소켓.output에 쓸때마다 초기화 한다. 그래야 보내고 받을때까지 기다려 준다.
					byte[] readBytes = new byte[4000];
					while (true) {
						if(socket.isClosed() || !socket.isConnected() || socket.isInputShutdown())
							break;
						int readableBytes = socket.getInputStream().available();
						if(readableBytes<0)
							break;
						if(System.currentTimeMillis() - readStartTime > readTimeout) {
							System.out.println("READ-TIME-OUT!!!");
							break;
						}
						if(readableBytes==0)
							continue;
						socket.getInputStream().read(readBytes, 0, readableBytes);
						String recivedHex = getHexString(Arrays.copyOf(readBytes, readableBytes));
						System.out.println("SERVER INPUTSTREAM : " + recivedHex + " : " + new String(Arrays.copyOf(readBytes, readableBytes))); // getHexString(Arrays.copyOf(readBytes, readableBytes)));
						if(recivedHex.equals("30000130ee00000000000010008000b000000")) {
							socket.getOutputStream().write(getBytesFromHexString(RDP_RETURN_CONN_HEX_STRING));
							socket.getOutputStream().flush();
							System.out.println("SERVER OUTPUTSTREAM : 30000130ed00000123400021f080008000000");
						} else if(recivedHex.equals("300002b26e00000000000436f6f6b69653a206d737473686173683d6861656e670d0a010008000b000000")) {
							socket.getOutputStream().write(getBytesFromHexString(RDP_RETURN_CONN_HEX_STRING));
							socket.getOutputStream().flush();
							System.out.println("SERVER OUTPUTSTREAM : 30000130ed00000123400021f080008000000");
						}
					}
				} catch (IOException e) {
					// 소켓대기 reset... 다시 대기 함.
					// e.printStackTrace();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getInfoIpPortConn(String ipaddress, int port, String sendHexData, boolean isSeache) {
		StringBuffer sb = new StringBuffer(ipaddress).append(" : ").append(port).append("\t=> ");
		int readTimeout = READ_TIMEOUT;
		long readStartTime = System.currentTimeMillis(); // 소켓.output에 쓸때마다 초기화 한다. 그래야 보내고 받을때까지 기다려 준다.
		// Socket socket;
		try(Socket socket = new Socket()) {
			// socket = new Socket(ipaddress, CONN_TIMEOUT);
			SocketAddress socketAddress = new InetSocketAddress(ipaddress, port);
			socket.setTcpNoDelay(true);
			socket.setReuseAddress(true);
			socket.setSoTimeout(READ_TIMEOUT); // 자바에서는 socket.timeout / read.timeout 두개 혼용하여 사용함.
			socket.connect(socketAddress , CONN_TIMEOUT);
			byte[] sendBytes = getBytesFromHexString(sendHexData);
			socket.getOutputStream().write(sendBytes );
			socket.getOutputStream().flush();
			sb.append("\nSending : " + sendHexData);
			sb.append("\nSending : " + getHexString(sendBytes));
			byte[] readBytes = new byte[4000];
			while (true) {
				if(socket.isClosed() || !socket.isConnected() || socket.isInputShutdown())
					break;
				int readableBytes = socket.getInputStream().available();
				if(readableBytes<0)
					break;
				if(System.currentTimeMillis() - readStartTime > readTimeout) {
					sb.append("\nREAD-TIME-OUT!!!");
					break;
				}
				if(readableBytes==0)
					continue;
				socket.getInputStream().read(readBytes, 0, readableBytes);
				String recivedHex = getHexString(Arrays.copyOf(readBytes, readableBytes));
				sb.append(new String(Arrays.copyOf(readBytes, readableBytes)));
				sb.append(":::").append(recivedHex).append("\n");
				if(recivedHex.equals(RDP_RETURN_CONN_HEX_STRING)) {
					String foundRdpSvrInfo = "FOUND RDP SERVER : " + ipaddress + ":" + port;
					synchronized (foundRdpInfo) {
						foundRdpInfo.append(foundRdpSvrInfo).append("\n");
						foundRdpSvrCount++;
					}
					System.out.println(foundRdpSvrInfo);
					if(isSeache) {
						isFouncRdpSvr=true;
						break;
					}
					readStartTime = System.currentTimeMillis(); // 소켓.output에 쓸때마다 초기화 한다. 그래야 보내고 받을때까지 기다려 준다.
					String sendingHex = "16030300a9010000a503036200b07cb51612e338853ff6491d78540971f28c037caeb33458b1de93b7b37000002ac02cc02bc030c02f009f009ec024c023c028c027c00ac009c014c013009d009c003d003c0035002f000a010000520000000e000c0000093132372e302e302e31000500050100000000000a00080006001d00170018000b00020100000d001400120401050102010403050302030202060106030023000000170000ff01000100";
					sendBytes = getBytesFromHexString(sendingHex);
					socket.getOutputStream().write(sendBytes );
					socket.getOutputStream().flush();
					sb.append("\nSending Hex : " + sendingHex);
					sb.append("\nSending String : " + new String(sendBytes)); // getHexString(sendBytes));
				}
			}
		} catch (UnknownHostException e) {
			sb.append(e.getMessage()); //  e.printStackTrace();
		} catch (IOException e) {
			sb.append(e.getMessage()); // e.printStackTrace();
		}
		return sb.toString();
	}


	public static String getHexString(byte[] buf) {
		return new java.math.BigInteger(buf).toString(16);
	}

	public static byte[] getBytesFromHexString(String hexString) {
		return new java.math.BigInteger(hexString, 16).toByteArray();
	}
}
