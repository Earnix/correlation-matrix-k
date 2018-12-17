package com.earnix.eo.gui.correlation;


import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Double.NaN;

/**
 * Frame with example correlation data used as matrix input.
 */
@SuppressWarnings("RedundantArrayCreation")
public class Example
{
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(Example::launch);
	}
	
	private static void launch() {
		JFrame frame = new JFrame();
		frame.setTitle("Correlation Matrix K");
		frame.setSize(800, 800);
		frame.setResizable(true);
		frame.setLocationByPlatform(true);

		double [][] correlations = new double[][] {
				{ 1.0, NaN, -0.018409163780178296, NaN, -0.04195467750878402, 0.14580489215919248, NaN, NaN, -0.07853599597576347, NaN, -0.22153558088827635, -0.07816295323584746, -0.2005920225109053, -0.018731242426898897, -0.17216008061244964, -0.1745473363858994, 0.05121686231313857, 0.10536306875349587, -0.015249662718811513, -0.036308960569717 },
				{ NaN, 1.0, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN },
				{ -0.018409163780178296, NaN, 1.0, NaN, 0.03593145002583371, 0.022174369861375616, NaN, NaN, 0.033606939983488716, NaN, 0.0085381113606187, 0.0018198710591183522, -0.0025214824559354056, 5.322989333635109E-4, 0.3489223540503647, 0.3768550974175637, 0.06181896711742225, 0.006756157728290126, -0.033485436956531396, -0.08597741062660215 },
				{ NaN, NaN, NaN, 1.0, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN },
				{ -0.04195467750878402, NaN, 0.03593145002583371, NaN, 1.0, -0.005636468220432189, NaN, NaN, 0.06077668579507978, NaN, 0.00800706774156412, -0.003276829336541495, 0.03326710384805788, 0.001136445559074406, 0.04858597656886969, 0.03002901072199911, -0.014226606681702201, 0.003013108438710165, -1.660372629002586E-4, 0.017573726575370558 },
				{ 0.14580489215919248, NaN, 0.022174369861375616, NaN, -0.005636468220432189, 1.0, NaN, NaN, -0.02485495200612799, NaN, -0.06872042667679863, -0.017088303161462516, -0.12522532905472758, 0.0027643178503375886, -0.1585808247033144, -0.13599503532351584, 0.06458287545909795, 0.04711597577328273, -0.006081581388067074, 0.003816201455786137 },
				{ NaN, NaN, NaN, NaN, NaN, NaN, 1.0, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN },
				{ NaN, NaN, NaN, NaN, NaN, NaN, NaN, 1.0, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN },
				{ -0.07853599597576347, NaN, 0.033606939983488716, NaN, 0.06077668579507978, -0.02485495200612799, NaN, NaN, 1.0, NaN, 0.006773496862553285, 0.014493272249967439, 0.02255155285957154, -0.009427432523633284, 0.27063323892357694, 0.19740181884735636, -0.04283489606743075, -0.025404598466161256, 0.0021842231206120784, -5.464956654088636E-4 },
				{ NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, 1.0, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN },
				{ -0.22153558088827635, NaN, 0.0085381113606187, NaN, 0.00800706774156412, -0.06872042667679863, NaN, NaN, 0.006773496862553285, NaN, 1.0, 0.06906690329650172, 0.0543955780717472, 0.00602340969203945, 0.02705511405704307, 0.01494729341789734, -0.030037744855721357, -0.04344220101902751, 0.01627335369780335, 0.0598817721020341 },
				{ -0.07816295323584746, NaN, 0.0018198710591183522, NaN, -0.003276829336541495, -0.017088303161462516, NaN, NaN, 0.014493272249967439, NaN, 0.06906690329650172, 1.0, 0.025340146992182844, -0.08015154022494693, 0.014526955107143939, 0.008288435750546505, -0.024547887641065063, -0.00590149784117126, 0.011564692882239398, 0.032369945548317854 },
				{ -0.2005920225109053, NaN, -0.0025214824559354056, NaN, 0.03326710384805788, -0.12522532905472758, NaN, NaN, 0.02255155285957154, NaN, 0.0543955780717472, 0.025340146992182844, 1.0, 0.006618615116041189, 0.08291261056722625, 0.07634784902157939, -0.02374920992586016, -0.012520479396632384, 0.006595792190611038, -0.009138539856948647 },
				{ -0.018731242426898897, NaN, 5.322989333635109E-4, NaN, 0.001136445559074406, 0.0027643178503375886, NaN, NaN, -0.009427432523633284, NaN, 0.00602340969203945, -0.08015154022494693, 0.006618615116041189, 1.0, -0.013048732766257166, -0.003244159000108978, 0.012768773319600277, -0.4340303148947489, 0.0050482526912951145, 0.03722576221529989 },
				{ -0.17216008061244964, NaN, 0.3489223540503647, NaN, 0.04858597656886969, -0.1585808247033144, NaN, NaN, 0.27063323892357694, NaN, 0.02705511405704307, 0.014526955107143939, 0.08291261056722625, -0.013048732766257166, 1.0, 0.9289477989717044, -0.07621761976638522, 0.003189005100401371, -0.04336645315108394, -0.20864474949115727 },
				{ -0.1745473363858994, NaN, 0.3768550974175637, NaN, 0.8002901072199911, -0.13599503532351584, NaN, NaN, 0.19740181884735636, NaN, 0.01494729341789734, 0.008288435750546505, 0.07634784902157939, -0.003244159000108978, 0.9289477989717044, 1.0, -0.03677137409885663, 0.002314589815797527, -0.03242863286809801, -0.19114160277240705 },
				{ 0.05121686231313857, NaN, 0.5181896711742225, NaN, -0.014226606681702201, 0.06458287545909795, NaN, NaN, -0.04283489606743075, NaN, -0.030037744855721357, -0.024547887641065063, -0.02374920992586016, 0.012768773319600277, -0.07621761976638522, -0.03677137409885663, 1.0, 0.023548383501469084, 0.015025527398832263, 0.013657328715466582 },
				{ 0.10536306875349587, NaN, 0.006756157728290126, NaN, 0.003013108438710165, 0.04711597577328273, NaN, NaN, -0.025404598466161256, NaN, -0.04344220101902751, -0.00590149784117126, -0.012520479396632384, -0.4340303148947489, 0.003189005100401371, 0.002314589815797527, 0.023548383501469084, 1.0, -0.017485303976621274, -0.1406261618964699 },
				{ -0.015249662718811513, NaN, -0.033485436956531396, NaN, -1.660372629002586E-4, -0.006081581388067074, NaN, NaN, 0.0021842231206120784, NaN, 0.01627335369780335, 0.011564692882239398, 0.006595792190611038, 0.0050482526912951145, -0.04336645315108394, -0.03242863286809801, 0.015025527398832263, -0.017485303976621274, 1.0, 0.1395010488031064 },
				{ -0.036308960569717, NaN, -0.08597741062660215, NaN, 0.017573726575370558, 0.003816201455786137, NaN, NaN, -5.464956654088636E-4, NaN, 0.0598817721020341, 0.032369945548317854, -0.009138539856948647, 0.03722576221529989, -0.20864474949115727, -0.19114160277240705, 0.013657328715466582, -0.1406261618964699, 0.1395010488031064, 1.0 }
		};

		double[][] correlationsSqr = new double[][] {
				{ 1.0, 0.020662563380152453, 3.3889731108542847E-4, 0.05163788346883987, 0.0017601949648660676, 0.02125906657755375, 6.13099166797164E-4, 1.6479837191490353E-5, 0.006167902663905135, 0.13166367676959617, 0.04907801359950603, 0.006109447258549277, 0.04023715949501554, 3.5085944285525726E-4, 0.02963909335648516, 0.030466772639412318, 0.0026231669852029944, 0.011101376257153897, 2.3255221303750974E-4, 0.0013183406176532637 },
				{ 0.020662563380152453, 1.0, NaN, 0.03999903162867131, 1.6373995918379734E-4, 0.003951308137334179, 0.029424030056140082, 0.040856629173779455, 0.0023391428163364435, 0.02857850875273569, 0.017741629516732176, 0.0017225325917097523, 0.025315310980691987, 4.215504108844102E-4, 0.004279401454054096, 0.003332818710325277, 0.0010040353805929117, 0.002782927013047233, 1.170899079822131E-4, 2.748188219994215E-4 },
				{ 3.3889731108542847E-4, NaN, 1.0, NaN, 0.001291069100958985, 4.917026787490833E-4, NaN, NaN, 0.0011294264150538125, NaN, 7.289934560632612E-5, 3.311930671816553E-6, 6.357873775590044E-6, 2.833421544599314E-7, 0.12174680915604806, 0.1420197644496014, 0.0038215846954649333, 4.5645667249534394E-5, 0.0011212744881698385, 0.00739211513805536 },
				{ 0.05163788346883987, 0.03999903162867131, NaN, 1.0, 3.0316309425459345E-4, 0.002022618888542866, 0.09742571002495422, 0.018989523595019365, 9.884016527003702E-4, 0.0586461164755668, 0.007446708129033781, 6.727694905781817E-4, 0.006009779924576268, 2.2068515102612404E-4, 0.014344948116432391, 0.014177943211175097, 1.0336335418552956E-4, 0.0016056271260286002, 1.121853501543162E-4, 8.037763970391294E-4 },
				{ 0.0017601949648660676, 1.6373995918379734E-4, 0.001291069100958985, 3.0316309425459345E-4, 1.0, 3.176977399994201E-5, 1.5749645727174012E-7, 9.091750489268421E-4, 0.003693805536233852, 0.0018666613081056063, 6.411313381799675E-5, 1.0737610500818975E-5, 0.0011067001984374674, 1.2915085087399392E-6, 0.0023605971191507547, 9.017414849419376E-4, 2.023963376758537E-4, 9.078822463426408E-6, 2.7568372671409588E-8, 3.088358657458854E-4 },
				{ 0.02125906657755375, 0.003951308137334179, 4.917026787490833E-4, 0.002022618888542866, 3.176977399994201E-5, 1.0, 4.2157128621092554E-4, 0.006161875825978191, 6.177686392269258E-4, 0.013336112453320077, 0.004722497042641257, 2.9201010493804983E-4, 0.0156813830368648, 7.641453177695026E-6, 0.025147877963583327, 0.01849464963264432, 0.0041709478025653566, 0.002219915173068565, 3.698563217968384E-5, 1.4563393551144231E-5 },
				{ 6.13099166797164E-4, 0.029424030056140082, NaN, 0.09742571002495422, 1.5749645727174012E-7, 4.2157128621092554E-4, 1.0, 0.003159614121036189, 5.9116046668257855E-5, 0.1109975119343565, 1.714267943548535E-5, 1.6303355706111187E-4, 9.071928214290466E-5, 6.953293896819441E-5, 0.00338680525353206, 0.003677129241478519, 1.1251307664109705E-5, 2.4729555304098726E-6, 8.684625111327066E-6, 4.6501465716517516E-5 },
				{ 1.6479837191490353E-5, 0.50856629173779455, NaN, 0.018989523595019365, 9.091750489268421E-4, 0.006161875825978191, 0.003159614121036189, 1.0, 2.803430623372599E-5, 0.12885874178271722, 5.607841531638988E-5, 7.782504862243015E-5, 0.0035451990155947644, 3.5917721402341636E-4, 0.01914578909971541, 0.011175378160469469, 0.0031501725808676996, 4.718165401857525E-4, 8.153264527266779E-4, 0.004233756600878293 },
				{ 0.006167902663905135, 0.0023391428163364435, 0.0011294264150538125, 9.884016527003702E-4, 0.003693805536233852, 6.177686392269258E-4, 5.9116046668257855E-5, 2.803430623372599E-5, 1.0, 0.0027051872664031174, 4.5880259747019196E-5, 2.1005494051167624E-4, 5.085725363780494E-4, 8.887648398765862E-5, 0.07324235001026588, 0.03896747808424449, 0.0018348283211075947, 6.453936232268828E-4, 4.770830640616366E-6, 2.986575123106766E-7 },
				{ 0.13166367676959617, 0.02857850875273569, NaN, 0.0586461164755668, 0.0018666613081056063, 0.013336112453320077, 0.1109975119343565, 0.12885874178271722, 0.0027051872664031174, 1.0, 0.002926786626187583, 9.691032708769684E-4, 0.006069881900431287, 4.0757161124335224E-5, 0.0683978621170037, 0.0722305485227261, 0.001036672869464968, 0.0014809458002002735, 5.970287233085714E-5, 0.002332297435100691 },
				{ 0.04907801359950603, 0.017741629516732176, 7.289934560632612E-5, 0.007446708129033781, 6.411313381799675E-5, 0.004722497042641257, 1.714267943548535E-5, 5.607841531638988E-5, 4.5880259747019196E-5, 0.002926786626187583, 1.0, 0.0047702371309683205, 0.0029588789137595444, 3.628146431815478E-5, 7.319791966396095E-4, 2.2342158052071715E-4, 9.022661160174148E-4, 0.0018872248293775947, 2.6482204057381E-4, 0.00358582663007995 },
				{ 0.006109447258549277, 0.0017225325917097523, 3.311930671816553E-6, 6.727694905781817E-4, 1.0737610500818975E-5, 2.9201010493804983E-4, 1.6303355706111187E-4, 7.782504862243015E-5, 2.1005494051167624E-4, 9.691032708769684E-4, 0.0047702371309683205, 1.0, 6.421230495854332E-4, 0.006424269400431286, 2.1103242468497536E-4, 6.869816719093742E-5, 6.025987876383549E-4, 3.482767676934904E-5, 1.3374212146051857E-4, 0.0010478133748010629 },
				{ 0.04023715949501554, 0.025315310980691987, 6.357873775590044E-6, 0.006009779924576268, 0.0011067001984374674, 0.0156813830368648, 9.071928214290466E-5, 0.0035451990155947644, 5.085725363780494E-4, 0.006069881900431287, 0.0029588789137595444, 6.421230495854332E-4, 1.0, 4.3806066054288924E-5, 0.006874500991072518, 0.00582899405022188, 5.640249721025748E-4, 1.5676240432149603E-4, 4.350447462172556E-5, 8.3512910717039E-5 },
				{ 3.5085944285525726E-4, 4.215504108844102E-4, 2.833421544599314E-7, 2.2068515102612404E-4, 1.2915085087399392E-6, 7.641453177695026E-6, 6.953293896819441E-5, 3.5917721402341636E-4, 8.887648398765862E-5, 4.0757161124335224E-5, 3.628146431815478E-5, 0.006424269400431286, 4.3806066054288924E-5, 1.0, 1.702694268051934E-4, 1.0524567617988083E-5, 1.630415720873359E-4, 0.1883823142476349, 2.5484855235168368E-5, 0.0013857573725100491 },
				{ 0.02963909335648516, 0.004279401454054096, 0.12174680915604806, 0.014344948116432391, 0.0023605971191507547, 0.025147877963583327, 0.00338680525353206, 0.01914578909971541, 0.07324235001026588, 0.0683978621170037, 7.319791966396095E-4, 2.1103242468497536E-4, 0.006874500991072518, 1.702694268051934E-4, 1.0, 0.8629440132143742, 0.005809125562853275, 1.016975353038596E-5, 0.0018806492589051585, 0.04353263149022777 },
				{ 0.030466772639412318, 0.003332818710325277, 0.1420197644496014, 0.014177943211175097, 9.017414849419376E-4, 0.01849464963264432, 0.003677129241478519, 0.011175378160469469, 0.03896747808424449, 0.0722305485227261, 2.2342158052071715E-4, 6.869816719093742E-5, 0.00582899405022188, 1.0524567617988083E-5, 0.8629440132143742, 1.0, 0.001352133953118064, 5.357326015393629E-6, 0.0010516162296938865, 0.03653511231040465 },
				{ 0.0026231669852029944, 0.0010040353805929117, 0.938215846954649333, 1.0336335418552956E-4, 2.023963376758537E-4, 0.0041709478025653566, 1.1251307664109705E-5, 0.0031501725808676996, 0.0018348283211075947, 0.001036672869464968, 9.022661160174148E-4, 6.025987876383549E-4, 5.640249721025748E-4, 1.630415720873359E-4, 0.005809125562853275, 0.001352133953118064, 1.0, 5.545263655322613E-4, 2.2576647361305903E-4, 1.8652262764230808E-4 },
				{ 0.011101376257153897, 0.002782927013047233, 4.5645667249534394E-5, 0.0016056271260286002, 9.078822463426408E-6, 0.002219915173068565, 2.4729555304098726E-6, 4.718165401857525E-4, 6.453936232268828E-4, 0.0014809458002002735, 0.0018872248293775947, 3.482767676934904E-5, 1.5676240432149603E-4, 0.1883823142476349, 1.016975353038596E-5, 5.357326015393629E-6, 5.545263655322613E-4, 1.0, 3.057358551548477E-4, 0.019775717409732163 },
				{ 2.3255221303750974E-4, 1.170899079822131E-4, 0.0011212744881698385, 1.121853501543162E-4, 2.7568372671409588E-8, 3.698563217968384E-5, 8.684625111327066E-6, 8.153264527266779E-4, 4.770830640616366E-6, 5.970287233085714E-5, 2.6482204057381E-4, 1.3374212146051857E-4, 4.350447462172556E-5, 2.5484855235168368E-5, 0.0018806492589051585, 0.0010516162296938865, 2.2576647361305903E-4, 3.057358551548477E-4, 1.0, 0.019460542617166676 },
				{ 0.0013183406176532637, 2.748188219994215E-4, 0.00739211513805536, 8.037763970391294E-4, 3.088358657458854E-4, 1.4563393551144231E-5, 4.6501465716517516E-5, 0.004233756600878293, 2.986575123106766E-7, 0.002332297435100691, 0.00358582663007995, 0.0010478133748010629, 8.3512910717039E-5, 0.0013857573725100491, 0.04353263149022777, 0.03653511231040465, 1.8652262764230808E-4, 0.019775717409732163, 0.019460542617166676, 1.0 }
		};

		List<RowType> rowsTypes = java.util.Arrays.asList(
				RowType.NUMERIC,
				RowType.NOMINAL,
				RowType.NUMERIC,
				RowType.NUMERIC,
				RowType.NOMINAL,
				RowType.NUMERIC,
				RowType.NOMINAL,
				RowType.NUMERIC,
				RowType.NUMERIC,
				RowType.NOMINAL,
				RowType.NUMERIC,
				RowType.NUMERIC,
				RowType.NUMERIC,
				RowType.NUMERIC,
				RowType.NUMERIC,
				RowType.NOMINAL,
				RowType.NUMERIC,
				RowType.NUMERIC,
				RowType.NUMERIC,
				RowType.NUMERIC
		);

		List<String> rowsTitles = Arrays.asList(
				"Duration" + Collections.nCopies(1, "A"),
				"Method",
				"Year",
				"Amount",
				"Status",
				"Score",
				"Difference",
				"Value",
				"Accuracy",
				"Level",
				"Type",
				"Length",
				"Connection",
				"Time",
				"Area",
				"Quality",
				"Indicator",
				"Width",
				"Country",
				"Location"
		);

		CorrelationMatrix matrix = new CorrelationMatrix(rowsTypes, rowsTitles, correlations, correlationsSqr);
		matrix.setBackground(Color.WHITE);
		frame.getContentPane().add(matrix);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
