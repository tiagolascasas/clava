int main() {
	
	int a = 0;
	int b = 0;
	
	switch(a) {
		case 0:
			b = 1;
			break;
		case 1 ... 3:
			b = 2;
			break;
		default:
			b = 3;
	}
	
}