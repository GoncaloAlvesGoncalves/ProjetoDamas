public class Damas {
    private char[][] path; // Representação do tabuleiro
    public int selectLine, selectCol; // Peça selecionada
    private boolean player; // Jogador atual (true = brancas, false = pretas)

    Damas() {
        path = new char[8][8];
        player = true; // Brancas começam
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i % 2 == 0 && j % 2 != 0) || (i % 2 != 0 && j % 2 == 0)) {
                    if (i < 3) path[i][j] = 'b'; // Pretas
                    if (i > 4) path[i][j] = 'w'; // Brancas
                }
            }
        }
    }

    // Retorna o jogador atual
    public boolean Player() {
        return player;
    }

    // Verifica se a peça na posição pertence ao jogador atual
    public boolean isPlayer(int l, int c) {
        char valor = path[l][c];
        return (player && (valor == 'w'||valor == 'W')) || (!player && (valor == 'b'|| valor == 'B'));
    }

    // Retorna o valor de uma posição no tabuleiro
    public char getValue(int l, int c) {
        return path[l][c];
    }
    
    public void setValue(int l, int c, char n) {
        path[l][c] = n;
    }

    // Move uma peça, com suporte para captura
    public void moverPeca(int l, int c, int newL, int newC) {
        int meioL = (l + newL) / 2;
        int meioC = (c + newC) / 2;
        
        // Realiza o movimento
        path[newL][newC] = path[l][c];
        path[l][c] = '\0';
        
        // Verifica se uma peca chegou ao fim
        if(newL==0 && player) {
        	path[newL][newC] = 'W';
        }
        if(newL == 7 && !player) {
        	path[newL][newC] = 'B';
        }

        // Verifica se é uma captura
        if (Math.abs(newL - l) == 2 && Math.abs(newC - c) == 2) {
            path[meioL][meioC] = '\0'; // Remove a peça capturada
            if (temCapturasDisponiveis(newL, newC)) {
                return;
            }
        }
        
        // Verifica se é uma captura por uma dama
        if (path[newL][newC] == 'W' || path[newL][newC] == 'B') {
            int direcaoL = (newL > l) ? 1 : -1;
            int direcaoC = (newC > c) ? 1 : -1;
            int atualL = l + direcaoL;
            int atualC = c + direcaoC;

            while (atualL != newL && atualC != newC) {
                char peca = getValue(atualL, atualC);
                if ((player && (peca == 'b' || peca == 'B')) || (!player && (peca == 'w' || peca == 'W'))) {
                    path[atualL][atualC] = '\0'; // Remove a peça capturada
                    if (temCapturasDisponiveisDama(newL, newC)) {
                        return;
                    }
                    break; // Apenas a primeira peça capturada é removida
                }
                atualL += direcaoL;
                atualC += direcaoC;
            }
        }
        

        // Muda o jogador após o movimento
        player = !player;
    }

    // Seleciona uma peça
    public void selecionarPeca(int l, int c) {
        selectLine = l;
        selectCol = c;
    }

    // Verifica se o movimento é válido
    public boolean isMoveValid(int l, int c, int newL, int newC) {
        char valor = getValue(l, c);
        
        if(valor == 'B' || valor == 'W') {
        	return movimentoDama(l,c,newL,newC);
        }

        // Verifica se a posição inicial ou final são inválidas
        if (valor == '\0' || getValue(newL, newC) != '\0') 
        	return false;

        // Verifica se é um movimento de captura
        if (Math.abs(newL - l) == 2 && Math.abs(newC - c) == 2) {
            int meioL = (l + newL) / 2;
            int meioC = (c + newC) / 2;
            char capturado = getValue(meioL, meioC);
            // Valida se a peça capturada pertence ao oponente
            if ((player && (capturado == 'b' || capturado == 'B')) || (!player && (capturado == 'w' || capturado == 'W'))) 
            	return true;
        }

        // Permite movimentos simples apenas se não houver capturas obrigatórias
        if (Math.abs(newL - l) == 1 && Math.abs(newC - c) == 1) {
        	// Verifica se é para frente (brancas para linhas maiores, pretas para linhas menores)
            if ((player && newL <= l) || (!player && newL >= l)) {
                // Permite o movimento apenas se não houver capturas obrigatórias
                return !capturaObrigatoria();
            }
        }
        return false;
    }

    // Verifica se há movimentos válidos no tabuleiro
    public boolean Jogadas() {
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                if (isPlayer(l, c) && temMovimentoValido(l, c)) 
                	return true;
            }
        }
        return false;
    }

    // Verifica se uma peça tem movimentos válidos
    public boolean temMovimentoValido(int l, int c) {
        for (int newL = 0; newL < 8; newL++) {
            for (int newC = 0; newC < 8; newC++) {
                if (isMoveValid(l, c, newL, newC)) 
                	return true;
            }
        }
        return false;
    }

 // Verifica se ha uma captura
    public boolean capturaObrigatoria() {
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                if (isPlayer(l, c) && temCapturasDisponiveis(l, c)) 
                	return true;
            }
        }
        return capturaObrigatoriaDama();
    }


    // Verifica se uma peça pode realizar capturas
    public boolean temCapturasDisponiveis(int l, int c) {
        for (int dl = -2; dl <= 2; dl += 4) { // dl varia entre -2 e 2 (2 casas acima ou abaixo da posição atual)
            for (int dc = -2; dc <= 2; dc += 4) { // dc varia entre -2 e 2 (2 casas à esquerda ou à direita da posição atual)
                int newL = l + dl, newC = c + dc; // Calcula as coordenadas do destino da peça após uma captura
                if (newL >= 0 && newL < 8 && newC >= 0 && newC < 8) {
                    int meioL = (l + newL) / 2;
                    int meioC = (c + newC) / 2;
                    char capturado = getValue(meioL, meioC);
                    
                    // Verifica se a peça capturada pertence ao oponente e o destino está vazio
                    if (getValue(newL, newC) == '\0' && ((player && capturado == 'b') || (!player && capturado == 'w'))) {
                    	return true;
                    }
                    if (getValue(newL, newC) == '\0' && ((player && capturado == 'B') || (!player && capturado == 'W'))) { 	
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    // Verifica se uma peca chegou ao fim
    public boolean movimentoDama(int l, int c, int newL, int newC) {
        // Verifica se a posição final está fora do tabuleiro ou ocupada
        if (newL < 0 || newL >= 8 || newC < 0 || newC >= 8 || getValue(newL, newC) != '\0') {
            return false;
        }

        // Movimentos válidos para damas devem ser diagonais
        int deltaL = Math.abs(newL - l);
        int deltaC = Math.abs(newC - c);
        // A movimentação deve ser diagonal (mesma distância em linha e coluna)
        if (deltaL != deltaC) {
            return false;
        }
        
        // Verifica o caminho para capturas ou bloqueios
        int direcaoL = (newL > l) ? 1 : -1; // Direção da linha
        int direcaoC = (newC > c) ? 1 : -1; // Direção da coluna
        int capturas = 0; // Conta peças capturadas
        
        for (int i = 1; i < deltaL; i++) {
            int atualL= l + i * direcaoL;
            int atualC = c + i * direcaoC;
            char peca = getValue(atualL, atualC);

            // Se encontra outra peça, verifica se é captura
            if (peca != '\0') {
                if ((player && (peca == 'b' || peca == 'B')) || (!player && (peca == 'w' || peca == 'W'))) {
                    capturas++;
                } 
                else {
                    return false; // Bloqueio por peça do próprio jogador ou múltiplas peças no caminho
                }
            }
        }
        // Movimentos simples não são válidos se houver capturas obrigatórias
        if (capturas == 0) {
            return !capturaObrigatoria();
        }

        // Para capturas, deve haver exatamente uma peça adversária no caminho
        return capturas == 1;

    }
    
 // Verifica se uma dama tem capturas obrigatórias
    public boolean capturaObrigatoriaDama() {
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                char valor = getValue(l, c);
                // Verifica se é uma dama do jogador atual
                if ((player && valor == 'W') || (!player && valor == 'B')) {
                    if (temCapturasDisponiveisDama(l, c)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Verifica se uma dama específica pode realizar capturas
    public boolean temCapturasDisponiveisDama(int l, int c) {
        int[] direcoes = {-1, 1}; // Direções para movimentação diagonal
        for (int direcaoL : direcoes) {
            for (int diracaoC : direcoes) {
                int atualL = l + direcaoL;
                int atualC = c + diracaoC;
                while (atualL >= 0 && atualL < 8 && atualC >= 0 && atualC < 8) {
                    char peca = getValue(atualL, atualC);
                    // Se encontra uma peça
                    if (peca != '\0') {
                        // Verifica se é uma peça adversária
                        if ((player && (peca == 'b' || peca == 'B')) || (!player && (peca == 'w' || peca == 'W'))) {
                            int capturaL = atualL + direcaoL;
                            int capturaC = atualC + diracaoC;
                            // Verifica se o espaço após a captura está vazio e dentro do tabuleiro
                            if (capturaL >= 0 && capturaL < 8 && capturaC >= 0 && capturaC < 8 && getValue(capturaL, capturaC) == '\0') {
                                return true; // Captura disponível
                            }
                            break;
                        } else {
                            break; // Peça do mesmo jogador bloqueia
                        }
                    }
                    // Continua para a próxima casa na mesma direção
                    atualL += direcaoL;
                    atualC += diracaoC;
                }
            }
        }
        return false;
    }
}
