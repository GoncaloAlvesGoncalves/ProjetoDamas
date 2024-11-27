import pt.iscte.guitoo.Color;
import pt.iscte.guitoo.StandardColor;
import pt.iscte.guitoo.board.Board;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.File;
import java.util.Scanner;

public class View {
    Board board; // Interface gráfica do tabuleiro
    Damas damas; // Lógica do jogo
    Map<String, Damas> saveGames = new HashMap<>(); // Jogos salvos
    Timer autoTimer;

    
    // Construtor da interface
    View(Damas damas) {
        this.damas = damas;
        board = new Board("...", 8, 8, 60); // Cria um tabuleiro de 8x8
        atualizarTitulo(); // Define o título inicial baseado no jogador
        board.setIconProvider(this::icon); // Define os ícones das peças
        board.addMouseListener(this::click); // Adiciona suporte a cliques
        board.setBackgroundProvider(this::background); // Define a cor de fundo
        adicionarAcoes(); // Configura botões e ações
    }

    // Atualiza o título com base no jogador atual
    void atualizarTitulo() {
        String titulo = damas.Player() ? "Jogo das Damas: Brancas a Jogar" : "Jogo das Damas: Pretas a Jogar";
        board.setTitle(titulo);
    }

    // Define os ícones para as peças no tabuleiro
    String icon(int line, int col) {
        char valor = damas.getValue(line, col);
        return valor == 'b' ? "black.png" : valor == 'w' ? "white.png": valor == 'B' ? "d_black.png": valor == 'W' ? "d_white.png" : null;
    }

    void click(int line, int col) {
        if (damas.Jogadas()) {
            if (damas.isPlayer(line, col)) {
                // Seleciona a peça clicada
                damas.selecionarPeca(line, col);
            } else if (damas.isMoveValid(damas.selectLine, damas.selectCol, line, col)) {
                // Realiza o movimento
                damas.moverPeca(damas.selectLine, damas.selectCol, line, col);
                // Atualiza o título
                atualizarTitulo();
                // Reseta a seleção após o movimento
                damas.selectLine = 0;
                damas.selectCol = 0;
                // Atualiza o tabuleiro
                board.refresh();
            } 
        } else {
        	board.showMessage("Fim de jogo!");
        }
    }


    // Define as cores do tabuleiro (pretas, brancas, seleções e movimentos válidos)
    Color background(int line, int col) {
        boolean isBlackSquare = (line % 2 == 0 && col % 2 != 0) || (line % 2 != 0 && col % 2 == 0);
        if (!isBlackSquare) return StandardColor.WHITE;

        if (damas.isMoveValid(damas.selectLine, damas.selectCol, line, col)) 
        	return StandardColor.YELLOW;
        if (line == damas.selectLine && col == damas.selectCol) 
        	return StandardColor.SILVER;

        return StandardColor.BLACK;
    }

    // Ações de botões
    void adicionarAcoes() {
        board.addAction("auto", this::auto); // Jogar automaticamente
        board.addAction("stop", this::stop); // Para as jogadas automaticas
        board.addAction("random", this::random);  // Jogada aleatória
        board.addAction("new", this::novaPartida); // Nova partida
        board.addAction("save", this::save); // Salvar jogo
        board.addAction("load", this::load); // Carregar jogo
    }

    // Inicia uma nova partida
    void novaPartida() {
        new View(new Damas()).start();
    }

    // Jogada aleatória
    void random() {
        if (!damas.Jogadas()) {
        	board.showMessage("Fim de jogo!");
            return;
        }

        Random r = new Random();
        int line, col;

        // Tenta selecionar uma peça válida
        do {
            line = r.nextInt(8);
            col = r.nextInt(8);
        } 
        while (!damas.isPlayer(line, col) || !damas.temMovimentoValido(line, col));
        damas.selecionarPeca(line, col);

        // Tenta realizar um movimento válido
        do {
            line = r.nextInt(8);
            col = r.nextInt(8);
        } 
        while (!damas.isMoveValid(damas.selectLine, damas.selectCol, line, col));
        click(line, col); // Executa o movimento   
    }

    // Jogo automático
    void auto() {
    	String txt = board.promptText("Velocidade (1-3)");
    	int v;
    	if(txt ==null || txt.isEmpty()) {
    		return;
    	}
		try {
	        v = Integer.parseInt(txt); // Converte para inteiro
	    } 
		catch (NumberFormatException e) {
			v = 0; // Valor padrão se o input não for um número válido
	    }
    	if(v>0 && v<4) {
    		autoTimer  = new Timer(1000/ (int)Math.pow(10, v-1), new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (damas.Jogadas()) {
                        random();
                        board.refresh();
                    } else {
    					((Timer) e.getSource()).stop(); // Para o timer quando o jogo termina
    					autoTimer = null; // Limpa a referência ao timer
                    	board.showMessage("Fim de jogo!");
                    }
                }
            });
            autoTimer.start();
    	}
    	else {
        		board.showMessage("Velocidade invalida");
        		auto();
    	}

    }
    
    // Para o jogo automático
    void stop() {
        if (autoTimer != null) {
            autoTimer.stop();
            autoTimer = null; // Limpa a referência ao timer
            board.showMessage("Jogo automático parado.");
        } else {
            board.showMessage("Nenhum jogo automático em execução.");
        }
    }

    // Salvar jogo
    void save() {
        String nome = board.promptText("Nome do jogo:");
        if (nome != null) {
	        if (nomeValido(nome)) {
		        String caminhoArquivo = "src/files/"+nome+".txt";
		        try {
		        	File arquivo = new File(caminhoArquivo);
		            if (arquivo.createNewFile()) {
		            	PrintWriter writer = new PrintWriter(arquivo);
		            	for(int l = 0 ; l<8; l++) {
		            		for(int c = 0; c<8;c++) {
		            			if(damas.getValue(l, c)=='\0') {
		            				writer.write('0');
		            			}
		            			else {
		            				writer.write(damas.getValue(l, c));
		            			}
		                    	
		            		}
		            		writer.write("\n");
		            	}
		            	writer.close();
		            	board.showMessage("Jogo salvo com sucesso!");
		            } else {
		            	board.showMessage("O arquivo com esse nome já existe.");
		            	save();
		            }
		        	
		        }
		        catch(Exception e) {
		        	System.err.println(e);
		        }
	        }
	        else {
	        	board.showMessage("O nome do ficheiro não pode ser vazio ou conter algum dos carateres que se seguem [<>:\"/|?*.].");
	        	save();
	        }
        }
    }
    
    public boolean nomeValido(String nomeArquivo) {
        // Verificar caracteres inválidos com base em restrições comuns
        String caracteresInvalidos = "[<>:\"/\\\\|?*.]"; //caracteres proibidos
        if (nomeArquivo.trim().isEmpty()) {
            return false; // Nome não pode ser vazio
        }
        
        if (nomeArquivo.matches(".*" + caracteresInvalidos + ".*")) {
            return false; // Nome contém caracteres inválidos
        }

        // Teste se o nome é válido tentando criar um objeto File
        File file = new File(nomeArquivo+".txt");
        try {
            file.getCanonicalPath(); // Testa se o nome é válido no sistema
            return true;
        } catch (Exception e) {
            return false; // Nome não é válido
        }
    }
    
    /////////////
    // POR VER //
    /////////////
    
    // Carregar jogo
    void load() {
    	String nome = board.promptText("Nome do jogo:");
    	Damas newDamas = new Damas();
        try {
        	Scanner s = new Scanner(new File("src/files/"+nome+".txt"));
        	System.out.print(s.next().length());

        	for(int j = 0 ; j < 8; j++) {
            	String linha = s.next();
            	for(int i = 0 ; i < linha.length();i++) {
            		newDamas.setValue(j, i, linha.charAt(i));
                	//System.out.print((linha.charAt(i)));
            	}
            	//System.out.print("\n");
        	}
        	new View(newDamas).start();

        }
        catch(Exception e) {
        	System.err.println(e);
        }
    	
    	
    	
    	/*
    	
        if (!saveGames.containsKey(nome)) {
            board.showMessage("O jogo com o nome " + nome + " não existe.");
            load();
            return;
        }
        new View(saveGames.get(nome)).start();*/
    }

    // Inicia a interface gráfica
    void start() {
        board.open();
    }

    public static void main(String[] args) {
        new View(new Damas()).start();
    }
}
