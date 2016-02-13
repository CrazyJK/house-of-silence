package jk.kamoru.crazy.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MergeTitleOnCover {

	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		log.info("start make new feed image");

		String text = "싸이클, 육상부, 수영부 강사, 테니스부 등 스포츠 웨어 코스프레 4실전 땀투성이 플레이";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		Date date = new Date();

		// 저장할 파일명 생성
		File makeImage = new File("C:\\Users\\kamoru\\Pictures/myFeedImage_" + sdf.format(date) + ".jpg");

		log.info("saved New image name : {}", makeImage.toString());

		// 문구 작성 할 이미지 불러오기
		File loadImage = new File("C:\\Users\\kamoru\\Pictures/CWM-236.jpg");

		BufferedImage bi = null;
		try {
			bi = ImageIO.read(loadImage);
		} catch (IOException e) {
			log.info("이미지 불러오다가 에러 나쓔..ㅜㅜ");
			e.printStackTrace();
		}

		int imgWidth = bi.getWidth();
		int imgHeight = bi.getHeight();

		log.info("loadedImage width : {}, height : {}", imgWidth, imgHeight);

		Graphics2D g2 = bi.createGraphics();

		// text에 적용할 폰트 생성, 아래 폰트는 시스템에 설치 되어 있어야 사용할 수 있음
		Font font = new Font("나눔고딕코딩", Font.PLAIN, 32);

		// 가운데 정렬하기 위해, text의 width구하기
		FontRenderContext frc = new FontRenderContext(null, true, true);
		Rectangle2D r2 = font.getStringBounds(text, frc);
		int textWidth = (int) r2.getWidth();
		int textHeight = (int) r2.getHeight();
		log.info("Text width : {}, height : {}", textWidth, textHeight);
		
		// 입력하는 문자의 가용 넓이
		int textBound = imgWidth;
		int paddingleft = (textBound - textWidth) / 2;
		if (paddingleft < 0)
			paddingleft = 0;

		log.info("paddingleft : " + paddingleft);

		// 라운드 사각형 채우기
		int rectY = 10;
		int margin = 5;
		int arcRound = 10;
		g2.setColor(new Color(255, 255, 255, 200));
		g2.fillRoundRect(paddingleft - margin, 10, textWidth + 2*margin, textHeight + 2*margin, arcRound, arcRound);
		
		// 이미지에 텍스트 사입. (text,x축,y축)
		g2.setFont(font);
		g2.setColor(Color.BLACK);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, new Integer(140));
		g2.drawString(text, paddingleft, rectY + textHeight);

		g2.dispose();

		try {
			ImageIO.write(bi, "jpg", makeImage);
		} catch (IOException e) {
			log.info("새로운 이미지 저장하다가 에러 나쓔..ㅜㅜ");
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		log.info("end make image [{}ms]", (endTime - startTime) / 1000.0);

	}

}
