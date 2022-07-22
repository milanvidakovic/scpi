package scpi.rigol;

import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL11C.glTexSubImage2D;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.awt.image.BufferedImage;
import java.io.IOException;

import scpi.visual.Visual;

public class RigolVisual extends Visual {

	public RigolVisual() throws IOException {
		super(new DS1054Z("192.168.123.2", 5555));
	}
	
	@Override
	public void render() {
		try {
			int rgb;
			int red;
			int green;
			int blue;
			red   = 0xFF;
			green = 0xFF;
			blue  = 0x00;
			if (this.mode == LOAD_BITMAP) {
				BufferedImage img = scope.readBmp();
				int height = img.getHeight();
				int width = img.getWidth();
				pixelframebuffer.position(0);
				int addr = 0;
				for (int h = height - 1; h >= 0; h--) {
					for (int w = 0; w < width; w++) {
						rgb = img.getRGB(w, h);
						red = (rgb >> 16) & 0x000000FF;
						green = (rgb >> 8) & 0x000000FF;
						blue = (rgb) & 0x000000FF;
						
						pixelframebuffer.put(addr + 0 , (byte) red);
						pixelframebuffer.put(addr + 1 , (byte) green);
						pixelframebuffer.put(addr + 2 , (byte) blue);

						pixelframebuffer.put(addr + 3 , (byte) red);
						pixelframebuffer.put(addr + 4 , (byte) green);
						pixelframebuffer.put(addr + 5 , (byte) blue);

						pixelframebuffer.put(addr + 2*800 * 3 + 0 , (byte) red);
						pixelframebuffer.put(addr + 2*800 * 3 + 1 , (byte) green);
						pixelframebuffer.put(addr + 2*800 * 3 + 2 , (byte) blue);

						pixelframebuffer.put(addr + 2*800 * 3 + 3 , (byte) red);
						pixelframebuffer.put(addr + 2*800 * 3 + 4 , (byte) green);
						pixelframebuffer.put(addr + 2*800 * 3 + 5 , (byte) blue);
						
						addr += 6;
					}
					addr += 2*800 * 3;
				}
			} else {
				scope.readRawData();
				
				if (counter++ == 2) {
					counter = 0;
					pixelframebuffer.position(0);
					backgroundbuffer.position(0);
					for (int i = 0; i < backgroundbuffer.limit(); i++) {
						pixelframebuffer.put(backgroundbuffer.get());
					}
				}
				pixelframebuffer.position(0);
				int x = 0;
				for (double i = 0; i < scope.data.length; i++, x = (int)(i / (scope.data.length) * 2*800)) {
					int y = scope.data[(int) i] + 256;
					
					int addr = (int)((x * 3) + (479 * 2*800 * 3) + y * (2*800 * 3));

					pixelframebuffer.put(addr + 0, (byte)red);
					pixelframebuffer.put(addr + 1, (byte)green);
					pixelframebuffer.put(addr + 2, (byte)blue);
				}
				
			}
			
			pixelframebuffer.position(0);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ################## STEFANOV KOD ##########################
		graphicsModeShader.use();
		graphicsModeShader.setInt("tex", 0);

		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 2*800, 2*480, GL_RGB, GL_UNSIGNED_BYTE, pixelframebuffer);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, graphicsModeTextureId[0]);

		glBindVertexArray(graphicsModeVAO[0]);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		// ################## STEFANOV KOD ##########################

		glBindVertexArray(0);
		glUseProgram(0);
	}

	@Override
	public void initBackground() {
		int red;
		int green;
		int blue;
		for (int y = 0; y < 2*480; y++) {
			for (int x = 0; x < 2*800; x++) {
				if (x % 133 == 0 || y % 120 == 0) {
					int addr = (int)((x * 3) + y * (2*800 * 3));
		
					if (x == 798 || y == 480) {
						red   = 0xDD;
						green = red;
						blue  = red;
					} else {
						red   = 0x66;
						green = red;
						blue  = red;
					}
					backgroundbuffer.put(addr + 0, (byte)red);
					backgroundbuffer.put(addr + 1, (byte)green);
					backgroundbuffer.put(addr + 2, (byte)blue);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			new RigolVisual();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
