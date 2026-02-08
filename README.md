# sc24

Cliente/Servidor em Java para troca de mensagens (strings) via sockets.

## Compilar

```bash
javac Server.java Client.java
```

## Executar

Em um terminal:

```bash
java Server 5000
```

Em outro terminal:

```bash
java Client localhost 5000
```

Digite mensagens em qualquer lado e pressione Enter. Para encerrar a conexão, digite `exit`.