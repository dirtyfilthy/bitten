/**
 * Copyright 2011 Micheal Swiggs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.bitcoin.core;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * SeedPeers stores a pre-determined list of Bitcoin node addresses. These nodes are selected based on being
 * active on the network for a long period of time. The intention is to be a last resort way of finding a connection
 * to the network, in case IRC and DNS fail. The list comes from the Bitcoin C++ source code.
 */
public class SeedPeers implements PeerDiscovery {
	private NetworkParameters params;
	private int pnseedIndex;
	
	public SeedPeers(NetworkParameters params) {
		this.params = params;
	}
	
	/**
	 * Acts as an iterator, returning the address of each node in the list sequentially.
	 * Once all the list has been iterated, null will be returned for each subsequent query.
     *
	 * @return InetSocketAddress - The address/port of the next node.
	 * @throws PeerDiscoveryException
	 */
	public InetSocketAddress getPeer() throws PeerDiscoveryException {
		try {
			return nextPeer();
		} catch (UnknownHostException e) {
			throw new PeerDiscoveryException(e);
		}
	}
	
	private InetSocketAddress nextPeer() throws UnknownHostException {
		if(pnseedIndex >= seedAddrs.length)return null;
		return new InetSocketAddress(convertAddress(seedAddrs[pnseedIndex++]),
				params.port);
	}

	/** Returns an array containing all the Bitcoin nodes within the list. */
	public InetSocketAddress[] getPeers() throws PeerDiscoveryException {
		try {
			return allPeers();
		} catch (UnknownHostException e) {
			throw new PeerDiscoveryException(e);
		}
	}

	private InetSocketAddress[] allPeers() throws UnknownHostException  {
		InetSocketAddress[] addresses = new InetSocketAddress[seedAddrs.length];
		for(int i=0;i < seedAddrs.length; ++i) {
			addresses[i] = new InetSocketAddress(convertAddress(seedAddrs[i]), params.port);
		}
		return addresses;
	}
	
	private InetAddress convertAddress(int seed) throws UnknownHostException{
		byte[] v4addr = new byte[4];
		v4addr[0] = (byte) (0xFF & (seed));
		v4addr[1] = (byte) (0xFF & (seed >> 8));
		v4addr[2] = (byte) (0xFF & (seed >> 16));
		v4addr[3] = (byte) (0xFF & (seed >> 24));
		return InetAddress.getByAddress(v4addr);
	}
	
	static int[] seedAddrs =
	{
		0x6884ac63, 0x3ffecead, 0x2919b953, 0x0942fe50, 0x7a1d922e, 0xcdd6734a, 0x953a5bb6, 0x2c46922e,
	    0xe2a5f143, 0xaa39103a, 0xa06afa5c, 0x135ffd59, 0xe8e82863, 0xf61ef029, 0xf75f042e, 0x2b363532,
	    0x29b2df42, 0x16b1f64e, 0xd46e281b, 0x5280bf58, 0x60372229, 0x1be58e4f, 0xa8496f45, 0x1fb1a057,
	    0x756b3844, 0x3bb79445, 0x0b375518, 0xcccb0102, 0xb682bf2e, 0x46431c02, 0x3a81073a, 0xa3771f1f,
	    0x213a121f, 0x85dc2c1b, 0x56b4323b, 0xb34e8945, 0x3c40b33d, 0xfa276418, 0x1f818d29, 0xebe1e344,
	    0xf6160a18, 0xf4fa384a, 0x34b09558, 0xb882b543, 0xe3ce2253, 0x6abf56d8, 0xe91b1155, 0x688ee6ad,
	    0x2efc6058, 0x4792cd47, 0x0c32f757, 0x4c813a46, 0x8c93644a, 0x37507444, 0x813ad218, 0xdac06d4a,
	    0xe4c63e4b, 0x21a1ea3c, 0x8d88556f, 0x30e9173a, 0x041f681b, 0xdc77ba50, 0xc0072753, 0xceddd44f,
	    0x052d1743, 0xe3c77a4a, 0x13981c3a, 0x5685d918, 0x3c0e4e70, 0x3e56fb54, 0xb676ae0c, 0xac93c859,
	    0x22279f43, 0x975a4542, 0xe527f071, 0xea162f2e, 0x3c65a32e, 0x5be5713b, 0x961ec418, 0xb202922e,
	    0x5ef7be50, 0xce49f53e, 0x05803b47, 0x8463b055, 0x78576153, 0x3ec2ae3a, 0x4bbd7118, 0xafcee043,
	    0x56a3e8ba, 0x6174de4d, 0x8d01ba4b, 0xc9af564e, 0xdbc9c547, 0xa627474d, 0xdada9244, 0xd3b3083a,
	    0x523e071f, 0xd6b96f18, 0xbd527c46, 0xdf2bbb4d, 0xd37b4a4b, 0x3a6a2158, 0xc064b055, 0x18a8e055,
	    0xec4dae3b, 0x0540416c, 0x475b4fbe, 0x064803b2, 0x48e9f062, 0x2898524b, 0xd315ff43, 0xf786d247,
	    0xc7ea2f3e, 0xc087f043, 0xc163354b, 0x8250284d, 0xed300029, 0xbf36e05c, 0x8eb3ae4c, 0xe7aa623e,
	    0x7ced0274, 0xdd362c1b, 0x362b995a, 0xca26b629, 0x3fc41618, 0xb97b364e, 0xa05b8729, 0x0f5e3c43,
	    0xdf942618, 0x6aeb9b5b, 0xbf04762e, 0xfaaeb118, 0x87579958, 0x76520044, 0xc2660c5b, 0x628b201b,
	    0xf193932e, 0x1c0ad045, 0xff908346, 0x8da9d4da, 0xed201c1f, 0xa47a2b1b, 0x330007d4, 0x8ba1ed47,
	    0xb2f02d44, 0x7db62c1b, 0x781c454b, 0xc0300029, 0xb7062a45, 0x88b52e3a, 0x78dd6b63, 0x1cb9b718,
	    0x5d358e47, 0x59912c3b, 0x79607544, 0x5197f759, 0xc023be48, 0xd1013743, 0x0f354057, 0x8e3aac3b,
	    0x4114693e, 0x22316318, 0xe27dda50, 0x878eac3b, 0x4948a21f, 0x5db7f24c, 0x8ccb6157, 0x26a5de18,
	    0x0a11bd43, 0x27bb1e41, 0x60a7a951, 0x3e16b35e, 0x07888b53, 0x5648a853, 0x0149fe50, 0xd070a34f,
	    0x6454c96d, 0xd6e54758, 0xa96dc152, 0x65447861, 0xf6bdf95e, 0x10400202, 0x2c29d483, 0x18174732,
	    0x1d840618, 0x12e61818, 0x089d3f3c, 0x917e931f, 0xd1b0c90e, 0x25bd3c42, 0xeb05775b, 0x7d550c59,
	    0x6cfacb01, 0xe4224444, 0xa41dd943, 0x0f5aa643, 0x5e33731b, 0x81036d50, 0x6f46a0d1, 0x7731be43,
	    0x14840e18, 0xf1e8d059, 0x661d2b1f, 0x40a3201b, 0x9407b843, 0xedf0254d, 0x7bd1a5bc, 0x073dbe51,
	    0xe864a97b, 0x2efd947b, 0xb9ca0e45, 0x4e2113ad, 0xcc305731, 0xd39ca63c, 0x733df918, 0xda172b1f,
	    0xaa03b34d, 0x7230fd4d, 0xf1ce6e3a, 0x2e9fab43, 0xa4010750, 0xa928bd18, 0x6809be42, 0xb19de348,
	    0xff956270, 0x0d795f51, 0xd2dec247, 0x6df5774b, 0xbac11f79, 0xdfb05c75, 0x887683d8, 0xa1e83632,
	    0x2c0f7671, 0x28bcb65d, 0xac2a7545, 0x3eebfc60, 0x304ad7c4, 0xa215a462, 0xc86f0f58, 0xcfb92ebe,
	    0x5e23ed82, 0xf506184b, 0xec0f19b7, 0x060c59ad, 0x86ee3174, 0x85380774, 0xa199a562, 0x02b507ae,
	    0x33eb2163, 0xf2112b1f, 0xb702ba50, 0x131b9618, 0x90ccd04a, 0x08f3273b, 0xecb61718, 0x64b8b44d,
	    0x182bf4dc, 0xc7b68286, 0x6e318d5f, 0xfdb03654, 0xb3272e54, 0xe014ad4b, 0x274e4a31, 0x7806375c,
	    0xbc34a748, 0x1b5ad94a, 0x6b54d10e, 0x73e2ae6e, 0x5529d483, 0x8455a76d, 0x99c13f47, 0x1d811741,
	    0xa9782a78, 0x0b00464d, 0x7266ea50, 0x532dab46, 0x33e1413e, 0x780d0c18, 0x0fb0854e, 0x03370155,
	    0x2693042e, 0xfa3d824a, 0x2bb1681b, 0x37ea2a18, 0x7fb8414b, 0x32e0713b, 0xacf38d3f, 0xa282716f,
	    0xb1a09d7b, 0xa04b764b, 0x83c94d18, 0x05ee4c6d, 0x0e795f51, 0x46984352, 0xf80fc247, 0x3fccb946,
	    0xd7ae244b, 0x0a8e0a4c, 0x57b141bc, 0x3647bed1, 0x1431b052, 0x803a8bbb, 0xfc69056b, 0xf5991862,
	    0x14963b2e, 0xd35d5dda, 0xc6c73574, 0xc8f1405b, 0x0ca4224d, 0xecd36071, 0xa9461754, 0xe7a0ed72,
	    0x559e8346, 0x1c9beec1, 0xc786ea4a, 0x9561b44d, 0x9788074d, 0x1a69934f, 0x23c5614c, 0x07c79d4b,
	    0xc7ee52db, 0xc72df351, 0xcb135e44, 0xa0988346, 0xc211fc4c, 0x87dec34b, 0x1381074d, 0x04a65cb7,
	    0x4409083a, 0x4a407a4c, 0x92b8d37d, 0xacf50b4d, 0xa58aa5bc, 0x448f801f, 0x9c83762e, 0x6fd5734a,
	    0xfe2d454b, 0x84144c55, 0x05190e4c, 0xb2151448, 0x63867a3e, 0x16099018, 0x9c010d3c, 0x962d8f3d,
	    0xd51ee453, 0x9d86801f, 0x68e87b47, 0x6bf7bb73, 0x5fc7910e, 0x10d90118, 0x3db04442, 0x729d3e4b,
	    0xc397d842, 0x57bb15ad, 0x72f31f4e, 0xc9380043, 0x2bb24e18, 0xd9b8ab50, 0xb786801f, 0xf4dc4847,
	    0x85f4bb51, 0x4435995b, 0x5ba07e40, 0x2c57392e, 0x3628124b, 0x9839b64b, 0x6fe8b24d, 0xaddce847,
	    0x75260e45, 0x0c572a43, 0xfea21902, 0xb9f9742e, 0x5a70d443, 0x8fc5910e, 0x868d4744, 0x56245e02,
	    0xd7eb5f02, 0x35c12c1b, 0x4373034b, 0x8786554c, 0xa6facf18, 0x4b11a31f, 0x3570664e, 0x5a64bc42,
	    0x0b03983f, 0x8f457e4c, 0x0fd874c3, 0xb6cf31b2, 0x2bbc2d4e, 0x146ca5b2, 0x9d00b150, 0x048a4153,
	    0xca4dcd43, 0xc1607cca, 0x8234cf57, 0x9c7daead, 0x3dc07658, 0xea5c6e4c, 0xf1a0084e, 0x16d2ee53,
	    0x1b849418, 0xfe913a47, 0x1e988f62, 0x208b644c, 0xc55ee980, 0xbdbce747, 0xf59a384e, 0x0f56091b,
	    0x7417b745, 0x0c37344e, 0x2c62ab47, 0xf8533a4d, 0x8030084d, 0x76b93c4b, 0xda6ea0ad, 0x3c54f618,
	    0x63b0de1f, 0x7370d858, 0x1a70bb4c, 0xdda63b2e, 0x60b2ba50, 0x1ba7d048, 0xbe1b2c1b, 0xabea5747,
	    0x29ad2e4d, 0xe8cd7642, 0x66c80e18, 0x138bf34a, 0xc6145e44, 0x2586794c, 0x07bc5478, 0x0da0b14d,
	    0x8f95354e, 0x9eb11c62, 0xa1545e46, 0x2e7a2602, 0x408c9c3d, 0x59065d55, 0xf51d1a4c, 0x3bbc6a4e,
	    0xc71b2a2e, 0xcdaaa545, 0x17d659d0, 0x5202e7ad, 0xf1b68445, 0x93375961, 0xbd88a043, 0x066ad655,
	    0x890f6318, 0x7b7dca47, 0x99bdd662, 0x3bb4fc53, 0x1231efdc, 0xc0a99444, 0x96bbea47, 0x61ed8748,
	    0x27dfa73b, 0x8d4d1754, 0x3460042e, 0x551f0c4c, 0x8d0e0718, 0x162ddc53, 0x53231718, 0x1ecd65d0,
	    0x944d28bc, 0x3b79d058, 0xaff97fbc, 0x4860006c, 0xc101c90e, 0xace41743, 0xa5975d4c, 0x5cc2703e,
	    0xb55a4450, 0x02d18840, 0xee2765ae, 0xd6012fd5, 0x24c94d7d, 0x8c6eec47, 0x7520ba5d, 0x9e15e460,
	    0x8510b04c, 0x75ec3847, 0x1dfa6661, 0xe172b3ad, 0x5744c90e, 0x52a0a152, 0x8d6fad18, 0x67b74b6d,
	    0x93a089b2, 0x0f3ac5d5, 0xe5de1855, 0x43d25747, 0x4bad804a, 0x55b408d8, 0x60a36441, 0xf553e860,
	    0xdb2fa2c8, 0x03152b32, 0xdd27a7d5, 0x3116a8b8, 0x0a1d708c, 0xeee2f13c, 0x6acf436f, 0xce6eb4ca,
	    0x101cd3d9, 0x1c48a6b8, 0xe57d6f44, 0x93dcf562,
	};
}
