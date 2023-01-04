import java.io.*;
import java.time.LocalTime;
import java.util.*;

//mangler txt-filene pga for stor størrelse (noder.txt, kanter.txt, interessepkt.txt)
//de kan lastes ned fra filen til oppgaveteksten
public class Dijkstra_ALT {

    public static void main(String[] args) {
        Graph graf = new Graph();

        System.out.println("leser inn node liste");
        graf.lagNoder("src/noder.txt");
        System.out.println("leser inn kant liste");
        graf.lagKanter("src/kanter.txt");
        System.out.println("leser inn interessepunkt liste");
        graf.lagInteressepkt("src/interessepkt.txt");
        System.out.println("ferdig med innlesning");

        start(graf);

    }

    public static int showMenu(Scanner in) {
        System.out.println("\n1. Preprosessering av noder");
        System.out.println("2. Kjør ALT");
        System.out.println("3. Kjør Dijkstra");
        System.out.println("4. Mål forskjell mellom ALT og Djikstra");
        System.out.println("5. Finn interessepunkter");
        System.out.println("6. Avslutt");

        return Integer.parseInt(in.next());
    }

    public static void start(Graph graph){
        Scanner in = new Scanner(System.in);
        ALT alt = new ALT(graph);
        Dijkstra dj = new Dijkstra(graph);
        int valg;

        //kan endre start og slutt node her
        int start = 232073;
        int slutt = 2517988;

        while((valg = showMenu(in)) != 6){

            switch (valg) {
                case 1 :
                    preprossesering(alt, "src/preprosessering.txt");
                    break;

                case 2 :
                    kjørALT(alt, start, slutt);
                    break;

                case 3 :
                    kjørDjikstra(dj, start, slutt);
                    break;

                case 4 :
                    kjørDjOgALT(dj, alt, start, slutt);
                    break;

                case 5 :
                    finnInnteressepunkt(dj);
                    break;

                default :
                    System.out.println("Velg et tall mellom 1 og 6:)");
                    break;

            }
        }

    }

    public static void preprossesering(ALT alt, String filepath){
        alt.skrivPreprosessering(filepath, 3502145,25937,40401,10374);
    }

    public static void kjørALT(ALT alt, int start, int slutt){
        System.out.println("Kjører ALT..");
        alt.kjørALT(start, slutt);
        System.out.println(alt.reiserute.get(0).d.distanse);
        System.out.println("Antall noder i reiserute med ALT: " + alt.getReiserute().size());
        System.out.println("Beregnet kjøretid: " +alt.regnKjøretid(alt.node[slutt].d.distanse));
    }

    public static void kjørDjikstra(Dijkstra dj, int start, int slutt){
        System.out.println("Kjører dijkstra..");
        dj.kjørDijkstra(start, slutt);
        System.out.println(dj.reiserute.get(0).d.distanse);
        System.out.println("Antall noder i reiserute med Dijkstra: " + dj.getReiserute().size());
        System.out.println("Beregnet kjøretid: " + dj.regnKjøretid(dj.node[slutt].d.distanse));
    }

    public static void kjørDjOgALT(Dijkstra dj, ALT alt, int start, int slutt){
        System.out.println("Kjører ALT og dijkstra..");
        alt.lesPreprosessering("src/preprosessering.txt");

        long a = System.nanoTime();
        alt.ALT(start, slutt);
        System.out.println("Tid brukt i millisekunder på ALT: "+ (System.nanoTime() - a)/1000000);

        alt.finnReiserute(alt.node[slutt]);
        System.out.println("Antall noder besøkt av ALT: " + alt.besøkteNoder.size());
        System.out.println("Antall noder i reiserute med ALT: " + alt.getReiserute().size());
        System.out.println();

        long d = System.nanoTime();
        dj.dijkstra(start, slutt);
        System.out.println("Tid brukt i millisekunder på dj: "+ (System.nanoTime() - d)/1000000);
        dj.finnReiserute(dj.node[slutt]);
        System.out.println("Antall noder besøkt av Djikstra: " + dj.besøkteNoder.size());
        System.out.println("Antall noder i reiserute med Djikstra: " + dj.getReiserute().size());
    }

    public static void finnInnteressepunkt(Dijkstra dj){
        System.out.println("Ladestasjoner nær Trondheim Lufthavn: " + dj.dijkstraEtterInteressePkf(7172108, 4, 8));
        System.out.println();
        System.out.println("Drikkesteder nær Trondheim torg: " + dj.dijkstraEtterInteressePkf(4546048, 16, 8));
        System.out.println();
        System.out.println("Ladestasjoner nær Hemsedal: " + dj.dijkstraEtterInteressePkf(3509663, 8, 8));
    }

}

class Graph {
    int N, K, I;
    Node[] node;

    public void initforgj(int s){
        for(int i = N; i-->0;){
            node[i].d = new Forgj();
            node[i].besøkt = false;
        }
        node[s].d.distanse = 0;
    }

    public void nullStillNoder(){
        for (Node n: node) {
            n.kant1 = null;
        }
    }

    public void lagTransponerteKanter(String filepath){
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))){
            FilBehandling.hsplit(br.readLine(), 1);
            K = Integer.parseInt(FilBehandling.felt[0]);
            for (int i = 0; i<K; i++){
                FilBehandling.hsplit(br.readLine(), 5);
                int til = Integer.parseInt(FilBehandling.felt[0]);
                int fra = Integer.parseInt(FilBehandling.felt[1]);
                int kjøretid = Integer.parseInt(FilBehandling.felt[2]);
                int lengde = Integer.parseInt(FilBehandling.felt[3]);
                int fartsgrense = Integer.parseInt(FilBehandling.felt[4]);
                Kant kant = new Kant(node[til], node[fra].kant1, kjøretid, lengde, fartsgrense);
                node[fra].kant1 = kant;
                node[fra].antallKanter++; //lagt til nå
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void lagNoder(String filepath){
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))){
            FilBehandling.hsplit(br.readLine(), 1);
            N = Integer.parseInt(FilBehandling.felt[0]);
            node = new Node[N];
            for (int i = 0; i<N; i++){
                FilBehandling.hsplit(br.readLine(), 3);
                int index = Integer.parseInt(FilBehandling.felt[0]);
                float breddegrad = Float.parseFloat(FilBehandling.felt[1]);
                float lengdegrad = Float.parseFloat(FilBehandling.felt[2]);
                node[i] = new Node(index, breddegrad, lengdegrad);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void lagKanter(String filepath){
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))){
            FilBehandling.hsplit(br.readLine(), 1);
            K = Integer.parseInt(FilBehandling.felt[0]);
            for (int i = 0; i<K; i++){
                FilBehandling.hsplit(br.readLine(), 5);
                int fra = Integer.parseInt(FilBehandling.felt[0]);
                int til = Integer.parseInt(FilBehandling.felt[1]);
                int kjøretid = Integer.parseInt(FilBehandling.felt[2]);
                int lengde = Integer.parseInt(FilBehandling.felt[3]);
                int fartsgrense = Integer.parseInt(FilBehandling.felt[4]);
                Kant kant = new Kant(node[til], node[fra].kant1, kjøretid, lengde, fartsgrense);
                node[fra].kant1 = kant;
                node[fra].antallKanter++; //lagt til nå
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void lagInteressepkt(String filepath){
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))){
            StringTokenizer st = new StringTokenizer(br.readLine());
            I = Integer.parseInt(st.nextToken());
            for (int i = 0; i<I; i++){
                st = new StringTokenizer(br.readLine());
                int nodenr = Integer.parseInt(st.nextToken());
                int kode = Integer.parseInt(st.nextToken());
                StringBuilder navn = new StringBuilder(st.nextToken());
                while (st.hasMoreTokens()){
                    navn.append(" ").append(st.nextToken());
                }
                node[nodenr].setInteressepunkt(kode,navn.toString());
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

class ALT {
    PriorityQueue<Node> queue;
    ArrayList<Node> besøkteNoder;
    ArrayList<Node> reiserute;
    Node[] node;
    Graph graf;
    int[][] avstanderTilLandemerke;
    int[][] avstanderFraLandemerke;

    public ALT(Graph graf){
        this.node = graf.node.clone();
        this.graf = graf;
    }

    public ArrayList<Node> ALT(int start, int slutt) {
        besøkteNoder = new ArrayList<>();
        Node sluttNode = node[slutt];
        this.queue = new PriorityQueue<>((a,b) -> ((a.sumAvstand()) - (b.sumAvstand())));
        graf.initforgj(start);
        queue.add(node[start]);

        while (!queue.isEmpty()){
            Node n = queue.poll();
            besøkteNoder.add(n);
            if (n == sluttNode) break;

            for (Kant k = n.kant1; k != null; k = k.neste){
                forkortALT(n,k, sluttNode);
            }
        }
        return besøkteNoder;
    }

    public void kjørALT(int start, int slutt){
        lesPreprosessering("src/preprosessering.txt");
        ALT(start, slutt);
        finnReiserute(node[slutt]);
    }

    public void finnReiserute(Node n){
        reiserute = new ArrayList<>();
        Node temp = n;
        reiserute.add(n);
        while ((temp = temp.d.forgj) != null){
            reiserute.add(temp);
        }
    }

    public ArrayList<Node> getReiserute(){
        return reiserute;
    }

    public void skrivReiseruteTilFil(){
        FilBehandling.skrivReiseruteTilFil("src/reiserute.txt", reiserute);
    }

    public LocalTime regnKjøretid(int distanse){
        return LocalTime.ofSecondOfDay(distanse/100);
    }

    public void forkortALT(Node n, Kant k, Node mål){
        Forgj nd = n.d, md = k.til.d;
        if (md.distanse > nd.distanse + k.vekt){
            md.distanse = nd.distanse + k.vekt;
            md.forgj = n;
            if (!k.til.besøkt){
                k.til.d.sumDistanse = beregnBestDist(k.til.index, mål.index);
                k.til.avstandTilMål = (int) regnUtDistanse(k.til, mål);
            }
            this.queue.add(k.til);
        }
    }

    private int beregnDist(int nIndex, int målIndex, int k){
        int dist = avstanderFraLandemerke[målIndex][k] - avstanderFraLandemerke[nIndex][k];
        int dist2 = avstanderTilLandemerke[nIndex][k] - avstanderTilLandemerke[målIndex][k];
        int result = Math.max(dist, dist2);
        return Math.max(result, 0);
    }

    private int beregnBestDist(int nIndex, int målIndex){
        int resultat = Math.max(beregnDist(nIndex, målIndex, 0), beregnDist(nIndex, målIndex, 1));
        resultat = Math.max(beregnDist(nIndex, målIndex, 2), resultat);
        resultat = Math.max(beregnDist(nIndex, målIndex, 3), resultat);
        return resultat;
    }

    private double radian(double tall){
        return tall * Math.PI/180;
    }

    public double regnUtDistanse(Node a, Node b) {
        double b1 = radian(a.bg);
        double b2 = radian(b.bg);
        double l1 = radian(a.lg);
        double l2 = radian(b.lg);

        return 2 * 6371f * Math.asin(Math.sqrt(Math.pow(Math.sin((b1 - b2) / 2), 2)
                        +
                        Math.cos(b1) * Math.cos(b2) * Math.pow(Math.sin((l1 - l2) / 2), 2)
                )
        );
    }

    public void avstandFraLandemerke(int landemerke, int index, Dijkstra dj) {
        System.out.println("Holder på med landemerke: " + landemerke);
        dj.dijkstra(landemerke);
        for(Node n : dj.node){
            avstanderFraLandemerke[n.index][index] = n.d.distanse;
        }
    }

    public void avstandFra4landemerker(int landemerke1, int landemerke2, int landemerke3, int landemerke4, Dijkstra dj){
        avstandFraLandemerke(landemerke1, 0,dj);
        avstandFraLandemerke(landemerke2,  1,dj);
        avstandFraLandemerke(landemerke3,  2,dj);
        avstandFraLandemerke(landemerke4,  3,dj);
    }

    public void avstandTilLandemerke(int landemerke, int index, Dijkstra dj){
        System.out.println("Holder på med landemerke: " + landemerke);
        dj.dijkstra(landemerke);
        for(Node n : dj.node){
            avstanderTilLandemerke[n.index][index] = n.d.distanse;
        }
    }

    public void avstandTil4landemerker(int landemerke1, int landemerke2, int landemerke3, int landemerke4, Dijkstra dj){
        graf.nullStillNoder();
        graf.lagTransponerteKanter("src/kanter.txt");
        avstandTilLandemerke(landemerke1, 0, dj);
        avstandTilLandemerke(landemerke2, 1, dj);
        avstandTilLandemerke(landemerke3, 2, dj);
        avstandTilLandemerke(landemerke4,3, dj);
        graf.nullStillNoder();
        graf.lagKanter("src/kanter.txt");
    }

    //preprosseserte noder: 3502145,25937,40401,10374
    public void skrivPreprosessering(String filepath, int landemerke1, int landemerke2, int landemerke3, int landemerke4){
        Dijkstra dj = new Dijkstra(graf);
        avstanderFraLandemerke = new int[graf.N][4];
        avstanderTilLandemerke = new int[graf.N][4];

        avstandFra4landemerker(landemerke1,landemerke2,landemerke3,landemerke4,dj);
        avstandTil4landemerker(landemerke1,landemerke2,landemerke3,landemerke4,dj);

        FilBehandling.skrivTilFilPreprosessering(filepath, this);
    }

    public void lesPreprosessering(String filepath){
        avstanderTilLandemerke = new int[graf.N][4]; //??
        avstanderFraLandemerke = new int[graf.N][4]; //??

        FilBehandling.lagPreprosessering(filepath, this);
    }
}

class Dijkstra {
    ArrayList<Node> besøkteNoder, reiserute;
    PriorityQueue<Node> queue;
    Node[] node;
    Graph graf;

    public Dijkstra(Graph graf){
        this.node = graf.node.clone();
        this.graf = graf;
    }

    public ArrayList<Node> getReiserute(){
        return reiserute;
    }

    public ArrayList<Node> dijkstra(int start, int slutt){
        this.queue = new PriorityQueue<>((a,b) -> (a.d.distanse) - (b.d.distanse));
        besøkteNoder = new ArrayList<>();
        Node sluttNode = node[slutt];
        graf.initforgj(start);
        queue.add(node[start]);

        while (!queue.isEmpty()){
            Node n = queue.poll();
            besøkteNoder.add(n);
            if (n == sluttNode) break;

            for (Kant k = n.kant1; k != null; k = k.neste){
                forkort(n,k);
            }
        }

        return besøkteNoder;
    }

    public void kjørDijkstra(int start, int slutt){
        dijkstra(start, slutt);
        finnReiserute(node[slutt]);
    }

    public void finnReiserute(Node n){
        reiserute = new ArrayList<>();
        Node temp = n;
        reiserute.add(n);
        while ((temp = temp.d.forgj) != null){
            reiserute.add(temp);
        }
    }

    public void dijkstra(int s){
        this.queue = new PriorityQueue<>((a,b) -> (a.d.distanse) - (b.d.distanse));
        graf.initforgj(s);
        queue.add(node[s]);

        while (!queue.isEmpty()){
            Node n = queue.poll();
            for (Kant k = n.kant1; k != null; k = k.neste){
                forkort(n,k);
            }
        }
    }

    public ArrayList<Node> dijkstraEtterInteressePkf(int start, int ipkt, int antall){
        this.queue = new PriorityQueue<>((a,b) -> (a.d.distanse) - (b.d.distanse));
        ArrayList<Node> list = new ArrayList<>();
        graf.initforgj(start);
        queue.add(node[start]);

        while (!queue.isEmpty() && list.size() < antall){
            Node n = queue.poll();
            if (n.i != null && (n.i.kode & ipkt) == ipkt && !list.contains(n)) {
                list.add(n);
            }
            for (Kant k = n.kant1; k != null; k = k.neste){
                forkort(n,k);
            }
        }
        return list;
    }

    public LocalTime regnKjøretid(int distanse){
        return LocalTime.ofSecondOfDay(distanse/100);
    }

    public void forkort(Node n, Kant k){
        Forgj nd = n.d, md = k.til.d;
        if (md.distanse > nd.distanse + k.vekt){
            md.distanse = nd.distanse + k.vekt;
            md.forgj = n;
            this.queue.add(k.til);
        }
    }
}

class InteressePunkt {
    int kode;
    String navn;

    public InteressePunkt(int kode, String  navn){
        this.kode = kode;
        this.navn = navn;
    }

    public String toString(){
        return navn + " " + kode;
    }
}

class Kant {
    Kant neste;
    Node til;
    int vekt;
    int lengde;
    int fartsgrense;

    public Kant(Node til, Kant neste, int kjøretid, int lengde, int fartsgrense){
        this.til = til;
        this.neste = neste;
        this.vekt = kjøretid;
        this.lengde = lengde;
        this.fartsgrense = fartsgrense;
    }

    public Kant(Node til, Kant neste, int vekt){
        this.til = til;
        this.neste = neste;
        this.vekt = vekt;
    }
}

class Node {
    Kant kant1;
    int index, antallKanter;
    boolean besøkt;
    Forgj d;
    float bg, lg;
    InteressePunkt i;
    int avstandTilMål;

    public Node(){
        kant1 = null;
    }

    public Node(int index, float bg, float lg){
        this.index = index;
        this.bg = bg;
        this.lg = lg;
        kant1 = null;
    }

    public int totalAvstand(){
        return avstandTilMål + d.distanse;
    }

    public int sumAvstand(){
        return d.sumDistanse + d.distanse;
    }

    public void setInteressepunkt(int kode, String navn){
        this.i = new InteressePunkt(kode, navn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Float.compare(node.bg, bg) == 0 && Float.compare(node.lg, lg) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, bg, lg);
    }

    public String toString(){
        return i.navn;
    }
}

class Forgj {
    int distanse, sumDistanse;
    Node forgj;
    static int uendelig = 1000000000;

    public Forgj(){
        distanse = uendelig;
    }

    public int finn_dist(){
        return distanse;
    }

    public Node finnForgj(){
        return forgj;
    }
}

class FilBehandling {

    static String[] felt = new String[10]; //Max 10 felt i en linje
    static void hsplit(String linje, int antall) {
        int j = 0;
        int lengde = linje.length();
        for (int i = 0; i < antall; ++i) {
//Hopp over innledende blanke, finn starten på ordet
            while (linje.charAt(j) <= ' ') ++j;
            int ordstart = j;
//Finn slutten på ordet, hopp over ikke-blanke
            while (j < lengde && linje.charAt(j) > ' ') ++j;
            felt[i] = linje.substring(ordstart, j);
        }
    }

    public static void lagPreprosessering(String filepath, ALT alt){
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))){
            for (int i = 0; i< alt.graf.N; i++){
                hsplit(br.readLine(), 8);
                for (int j = 0; j<4; j++){
                    alt.avstanderFraLandemerke[i][j] = Integer.parseInt(felt[j]);
                    alt.avstanderTilLandemerke[i][j] = Integer.parseInt(felt[j+4]);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void skrivTilFilPreprosessering(String filepath, ALT alt){
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filepath))){

            for (int i = 0; i<alt.node.length; i++){
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j<4; j++){
                    sb.append(alt.avstanderFraLandemerke[i][j]).append(" ");
                }
                for (int j = 0; j<4; j++){
                    sb.append(alt.avstanderTilLandemerke[i][j]).append(" ");
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void skrivReiseruteTilFil(String filepath, List<Node> noder){
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filepath))){
            for (Node n: noder){
                bw.write(n.bg + "," + n.lg);
                bw.newLine();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}