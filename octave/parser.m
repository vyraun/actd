printf("Loading ...\n")
X = convToIndexed(csvread("../trace.csv"));
V = unique(X(:, 1));
n = size(V, 1);
printf("Writting ...\n")
for i = 1 : n
	Y = X(find(X(:, 1) == V(i)), 2 : end);
	csvwrite(["../trace-" num2str(i) ".csv"], Y);
endfor
