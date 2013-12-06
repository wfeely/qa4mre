function regression(labelfile, datafile)
    if nargin<2,
        labelfile = 'binary_labels';
        datafile = 'scores_sim_syn_pmi';
    end;
    
    %Y = dlmread(labelfile);
    data = dlmread(datafile,',');
    Y = data(:,1);
    X = data(:,[2,4]);
    [n,m] = size(X);
    %X = horzcat(ones(n,1), X);
    
    p = regress(Y,X)
    

end