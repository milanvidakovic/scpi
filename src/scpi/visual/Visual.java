package scpi.visual;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VERSION_UNAVAILABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowAspectRatio;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.glfw.GLFW.nglfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import scpi.Scope;

public abstract class Visual {
	public Scope scope;

	public ByteBuffer pixelframebuffer;
	public ByteBuffer backgroundbuffer;

	public long window;
	public int width;
	public int height;
	public int x = 100;
	public int y = 100;
	public GLFWErrorCallback errCallback;
	public GLFWKeyCallback keyCallback;
	public GLFWMouseButtonCallback mouseButtonCallback;
	public GLFWCursorPosCallback cursorPosCallback;
	public GLFWFramebufferSizeCallback fbCallback;
	public Callback debugProc;
	public int[] graphicsModeTextureId = new int[10];

	public int[] graphicsModeVAO = new int[10];// IntBuffer.allocate(1);
	public int[] graphicsModeVBO = new int[10];// IntBuffer.allocate(1);
	public int[] graphicsModeEBO = new int[10];// IntBuffer.allocate(1);

	public Shader graphicsModeShader;
	public String graphicsShaderVertSource = "#version 330 core\nlayout (location = 0) in vec3 aPos; layout (location = 1) in vec2 aTexCoord; out vec2 TexCoord; void main() { gl_Position = vec4(aPos, 1.0); TexCoord = aTexCoord; }";
	public String graphicsShaderFragSource = "#version 330 core\nout vec4 FragColor; in vec2 TexCoord; uniform sampler2D tex; void main() { FragColor = texture(tex, TexCoord); }";

	public Visual(Scope scope) {
		this.scope = scope;
		Thread t = new Thread() {
			public void run() {
				try {
					init();
					glfwSetWindowPos(window, x, y);
					loop();
					errCallback.free();
					keyCallback.free();
					mouseButtonCallback.free();
					cursorPosCallback.free();
					fbCallback.free();
					if (debugProc != null)
						debugProc.free();
					glfwDestroyWindow(window);
					scope.close();
				} catch (Throwable t) {
					t.printStackTrace();
				} finally {
					glfwTerminate();
				}
			}
		};
		t.start();
	}

	

	public static final int DRAW_WAVEFORM = 0;
	public static final int LOAD_BITMAP   = 1;
//	private int mode = DRAW_WAVEFORM;
	public int mode = LOAD_BITMAP;
	
	public int counter = 0;
	
	// ############################### OPENGL STUFF ##########################################
		
	public abstract void initBackground();
	
	public abstract void render();
	
	private void loop() {
		while (!glfwWindowShouldClose(window)) {
			glfwPollEvents();
			glViewport(0, 0, width, height);

			render();

			glfwSwapBuffers(window);
		}
	}
	
	private void init() throws IOException {
		
		this.width = 2*800;
		this.height = 2*480;
		
		this.pixelframebuffer = BufferUtils.createByteBuffer(2*800 * 2*480 * 3);
		this.backgroundbuffer = BufferUtils.createByteBuffer(2*800 * 2*480 * 3);
		
		initBackground();
		
		glfwSetErrorCallback(errCallback = new GLFWErrorCallback() {
			private GLFWErrorCallback delegate = GLFWErrorCallback.createPrint(System.err);

			@Override
			public void invoke(int error, long description) {
				if (error == GLFW_VERSION_UNAVAILABLE)
					System.err.println("This demo requires OpenGL 3.0 or higher.");
				delegate.invoke(error, description);
			}

			@Override
			public void free() {
				delegate.free();
			}
		});

		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		try {
			if (width == 0)
				width = 100;
			if (height == 0)
				height = 100;
			window = glfwCreateWindow(width, height, "FBViewer", NULL, NULL);
		} catch (Exception ex) {
			JOptionPane.showConfirmDialog(null, "Framebuffer window failed to open. Cause: " + ex.getMessage());
		}
		if (window == NULL) {
			throw new AssertionError("Failed to create the GLFW window");
		}

		glfwSetWindowAspectRatio(window, 5, 3);


		glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				if (width > 0 && height > 0 && (Visual.this.width != width || Visual.this.height != height)) {
					Visual.this.width = width;
					Visual.this.height = height;
				}
			}
		});

		glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {

				} else if (action == GLFW_RELEASE) {
					if (key == 257) { // VK_ENTER
						Visual.this.mode ^= 1;
					}
				}
			}
		});

		glfwSetMouseButtonCallback(window, mouseButtonCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				double[] xpos = new double[1];
				double[] ypos = new double[1];
				glfwGetCursorPos(window, xpos, ypos);

				if (action == GLFW.GLFW_PRESS) {
				} else {
				}
			}
		});

		glfwSetCursorPosCallback(window, cursorPosCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
			}
		});

		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
		glfwMakeContextCurrent(window);
		glfwSwapInterval(0);
		glfwShowWindow(window);

		try (MemoryStack frame = MemoryStack.stackPush()) {
			IntBuffer framebufferSize = frame.mallocInt(2);
			nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
			width = framebufferSize.get(0);
			height = framebufferSize.get(1);
		}

		GL.createCapabilities();
		debugProc = GLUtil.setupDebugMessageCallback();

		// ##################### STEFANOV KOD ##################################
		glGenTextures(graphicsModeTextureId);
		glBindTexture(GL_TEXTURE_2D, graphicsModeTextureId[0]);
		// Blending
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 2*800, 2*480, 0, GL_RGB, GL_UNSIGNED_BYTE, pixelframebuffer);

		glGenerateMipmap(GL_TEXTURE_2D);

		float vertices[] = {
				// positions // texture coords
				1.0f, 1.0f, 0.0f, 1.0f, 1.0f, // top right
				-1.0f, 1.0f, 0.0f, 0.0f, 1.0f, // top left
				1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // bottom right
				-1.0f, -1.0f, 0.0f, 0.0f, 0.0f // bottom left
		};

		int indices[] = { 0, 1, 2, 1, 2, 3 };

		glGenVertexArrays(graphicsModeVAO);
		glGenBuffers(graphicsModeVBO);
		glGenBuffers(graphicsModeEBO);

		glBindVertexArray(graphicsModeVAO[0]);

		// Graphics VBO
		glBindBuffer(GL_ARRAY_BUFFER, graphicsModeVBO[0]);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

		// Graphics EBO
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, graphicsModeEBO[0]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0L); // Vertex position
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4L); // Texture coords
		glEnableVertexAttribArray(1);

		// Shaders
		try {
			graphicsModeShader = new Shader(graphicsShaderVertSource, graphicsShaderFragSource);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ##################### STEFANOV KOD ##################################

	}

	@SuppressWarnings("unused")
	private void load(String fileName) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String s;
			List<Byte> list = new ArrayList<Byte>();
			while ((s = in.readLine()) != null) {
				s = s.trim();
				String[] tokens = s.split(",");
				for (String token : tokens) {
					list.add(Byte.parseByte(token.trim()));
				}
			}
			in.close();

			Byte[] buff = list.toArray(new Byte[0]);
			scope.data = ArrayUtils.toPrimitive(buff);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void loadBmp(String fileName) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(fileName));
		} catch (IOException e) {

		}
		int height = img.getHeight();
		int width = img.getWidth();

		int rgb;
		int red;
		int green;
		int blue;

		System.out.printf("width: %d, height: %d, pixelframebuffer.limit(): %d\n", width, height,
				pixelframebuffer.limit());

		pixelframebuffer.position(0);
		for (int h = height - 1; h >= 0; h--) {
			for (int w = 0; w < width; w++) {

				rgb = img.getRGB(w, h);
				red = (rgb >> 16) & 0x000000FF;
				green = (rgb >> 8) & 0x000000FF;
				blue = (rgb) & 0x000000FF;
				pixelframebuffer.put((byte) red);
				pixelframebuffer.put((byte) green);
				pixelframebuffer.put((byte) blue);
			}
		}
		pixelframebuffer.position(0);

	}

}
