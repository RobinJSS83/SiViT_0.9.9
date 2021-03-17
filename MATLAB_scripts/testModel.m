function [t, p, v, pnorm, vnorm, snames, rnames, vmin, vmax, pmin, pmax, optionObj] = testModel( file_name, sim_time )
%TESTMODEL Summary of this function goes here
%   Detailed explanation goes here

% load
sivit = sivitLoadSBMLModel(file_name);
% set options
optionObj = setMaxSimTime(sivit.m, sim_time);
% simulate
[t, p, v, pnorm, vnorm, snames, rnames, vmin, vmax, pmin, pmax] = calculateFlux(sivit.m, optionObj, sivit.time_symbol);

end

